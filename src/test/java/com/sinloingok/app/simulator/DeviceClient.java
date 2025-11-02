package com.sinloingok.app.simulator;

import com.sinloingok.app.constant.SignalTopology;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceClient {
    private final String host;
    private final int port;
    private final String onlyCode;

    private volatile Channel channel;
    private final AtomicBoolean roundRunning = new AtomicBoolean(false);
    private final AtomicBoolean linearRunning = new AtomicBoolean(false);
    private final Random rnd = new Random();
    private ScheduledFuture<?> roundFuture;
    private ScheduledFuture<?> linearFuture;
    private ScheduledFuture<?> heartbeatFuture;

    private DeviceClient pmDeviceClient;

    public DeviceClient(String host, int port, String onlyCode) {
        this.host = host;
        this.port = port;
        this.onlyCode = onlyCode;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap bs = new Bootstrap();
            bs.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) {
                            channel.pipeline().addLast(new DeviceHandler(DeviceClient.this));
                        }
                    });
            ChannelFuture f = bs.connect(host, port).sync();
            this.channel = f.channel();
            f.channel().closeFuture().sync();
        } finally {
            stopRound();
            channel = null;
            group.shutdownGracefully();
        }
    }

    // ========== exposed ==========
    public Channel channel() {
        return channel;
    }

    public boolean isRoundRunning() {
        return roundRunning.get();
    }

    public boolean isLinearRunning() {
        return linearRunning.get();
    }

    // 兼容服务端 substring(12,14),(14,16) 的开/关帧
    public void sendOpenCompat(int ch) {
        sendHex(SwitchProtoCompat.buildOpen(ch),
                "OPEN-compat ch=" + ch + " function_name=" + SignalTopology.getFunctionCode(ch));
    }

    public void sendCloseCompat(int ch) {
        sendHex(SwitchProtoCompat.buildClose(ch),
                "CLOSE-compat ch=" + ch + " function_name=" + SignalTopology.getFunctionCode(ch));
    }

    // 文档版（OH/OL）
    public void sendDocRising(int ch) {
        sendHex(SwitchFrameUtil.buildUploadFrame(ch, true),
                "RISING(doc) ch=" + ch + " function_name=" + SignalTopology.getFunctionCode(ch));
    }

    public void sendDocFalling(int ch) {
        sendHex(SwitchFrameUtil.buildUploadFrame(ch, false),
                "FALLING(doc) ch=" + ch + " function_name=" + SignalTopology.getFunctionCode(ch));
    }

    public void startRound(int times) {
        if (channel == null || !channel.isActive()) {
            System.out.println(onlyCode + " 未连接");
            return;
        }
        if (roundRunning.get()) {
            System.out.println(onlyCode + " 已在轮番中");
            return;
        }
        if (linearRunning.get()) {
            System.out.println(onlyCode + " 已在执行线性测试，先停止线性测试");
            return;
        }
        roundRunning.set(true);
        runRandom(times);
    }

    public void stopRound() {
        roundRunning.set(false);
        stopLinearTest();
        if (roundFuture != null) roundFuture.cancel(false);
        roundFuture = null;
        if (channel != null && channel.isActive()) {
            // 安全：全关（1..8），避免残留上一次状态
            for (int ch = 1; ch <= 8; ch++) {
                sendCloseCompat(ch);
                try { Thread.sleep(30); } catch (InterruptedException ignored) {}
            }
        }
    }

    public void startLinear(int loops) {
        if (channel == null || !channel.isActive()) {
            System.out.println(onlyCode + " 未连接");
            return;
        }
        if (linearRunning.get()) {
            System.out.println(onlyCode + " 线性测试已在执行");
            return;
        }
        if (roundRunning.get()) {
            System.out.println(onlyCode + " 正在轮番测试，请先 stop");
            return;
        }
        if (loops < 0) {
            loops = 0;
        }
        linearRunning.set(true);
        runLinear(loops);
    }

    // ========== internal ==========
    void onActive(ChannelHandlerContext ctx) {
        // 上线立即心跳一次 + 每秒心跳
        System.out.println(onlyCode + " 已连接 " + ctx.channel().remoteAddress());
        sendHeartbeat(ctx, "HB(ONLINE)");
        heartbeatFuture = ctx.channel().eventLoop().scheduleAtFixedRate(
                () -> sendHeartbeat(ctx, "HB(periodic)"),
                1000, 1000, TimeUnit.MILLISECONDS
        );
    }

    void onMessage(byte[] bytes) {
        // 可按需打印
        // System.out.println(onlyCode + " <<< RESP HEX=" + Hex.toHex(bytes));
    }

    void onInactive() {
        System.out.println(onlyCode + " 连接已断开");
        cancelHeartbeat();
        linearRunning.set(false);
        if (linearFuture != null) {
            linearFuture.cancel(false);
            linearFuture = null;
        }
        roundRunning.set(false);
        if (roundFuture != null) {
            roundFuture.cancel(false);
            roundFuture = null;
        }
    }

    // ========== 随机轮番（新版，已按 8↔1 对调语义） ==========
    // 通道定义（反转后）：
    //  1=PM(泡沫)  2=XS(洗手)  3=QS2(清水2)  4=CQ(吹气)
    //  5=DM(镀膜)  6=GJ(关机)  7=XC(吸尘)   8=QS1(清水1)
    //
    // 规则：每轮随机挑选若干通道（来自 {2,4,5,7,8} = XS、CQ、DM、XC、QS1），
    //      随机顺序逐个 sendOpenCompat(ch)；
    //      轮与轮之间随机间隔；若存在 PM 设备，则在该轮内穿插 0~N 次短促触发
    //      （pmDeviceClient.sendOpenCompat(pmCh)；pmCh 是 PMKZSB 的站点通道号）。
    private void runRandom(int times) {
        final int[] pool = {2, 4, 5, 7, 8}; // ★ 已调整：去掉 1，加入 5
        final EventLoop loop = channel.eventLoop();

        Runnable task = new Runnable() {
            int remain = times;

            @Override
            public void run() {
                if (!roundRunning.get() || channel == null || !channel.isActive()) return;

                // 1) 本轮随机选若干个主设备通道（至少1个，至多 pool.length）
                int count = 1 + rnd.nextInt(pool.length);   // 1..5 个
                int[] picks = randomPickAndShuffle(pool, count);

                // 2) 逐个触发（用 sendOpenCompat 表示“动作”，不区分语义）
                for (int ch : picks) {
                    sendOpenCompat(ch);
                    try { Thread.sleep(50 + rnd.nextInt(151)); } catch (InterruptedException ignored) {}
                }

                // 3) 穿插 PM 设备（0~2 次），短促触发 PMKZSB 相应通道
                if (pmDeviceClient != null && pmDeviceClient.channel() != null && pmDeviceClient.channel().isActive()) {
                    int pmTimes = rnd.nextInt(3); // 0、1 或 2 次
                    for (int i = 0; i < pmTimes; i++) {
                        int pmCh = SignalTopology.tempWscNetSitePmChNum(onlyCode); // PMKZSB.ch(siteNo)
                        pmDeviceClient.sendOpenCompat(pmCh);
                        try { Thread.sleep(200 + rnd.nextInt(601)); } catch (InterruptedException ignored) {}
                    }
                }

                // 4) 轮次控制
                if (remain > 0 && --remain <= 0) {
                    stopRound();
                    return;
                }

                // 5) 下一轮：3~10s 后继续
                int nextGap = 3 + rnd.nextInt(8);
                roundFuture = loop.schedule(this, nextGap, TimeUnit.SECONDS);
            }
        };

        // 立即启动第一轮
        roundFuture = loop.schedule(task, 0, TimeUnit.SECONDS);
    }

    private void runLinear(int loops) {
        final int[] sequence = {3, 1, 7, 5, 2, 8};
        final EventLoop loop = channel.eventLoop();

        Runnable task = new Runnable() {
            int index = 0;
            int remain = loops;

            @Override
            public void run() {
                if (!linearRunning.get() || channel == null || !channel.isActive()) {
                    stopLinearTest();
                    return;
                }

                int chNum = sequence[index];
                int holdSeconds = 5 + rnd.nextInt(11); // 5~15 秒
                long holdMillis = holdSeconds * 1000L;
                String funcCode = SignalTopology.getFunctionCode(chNum);
                String funcName = SignalTopology.getFunctionName(chNum);
                System.out.println(onlyCode + " >>> LINEAR ch=" + chNum +
                        " function=" + funcCode + (funcName != null ? "(" + funcName + ")" : "") +
                        " duration=" + holdSeconds + "s");
                sendOpenCompat(chNum);

                long closeDelay = Math.max(1000L, holdMillis - 500L);
                loop.schedule(() -> sendCloseCompat(chNum), closeDelay, TimeUnit.MILLISECONDS);

                index++;
                if (index >= sequence.length) {
                    index = 0;
                    if (remain > 0) {
                        remain--;
                        if (remain == 0) {
                            System.out.println(onlyCode + " 线性测试完成");
                            stopLinearTest();
                            return;
                        }
                    }
                }

                long nextDelay = holdMillis + 500L;
                linearFuture = loop.schedule(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        };

        linearFuture = loop.schedule(task, 0, TimeUnit.SECONDS);
    }

    private void stopLinearTest() {
        linearRunning.set(false);
        if (linearFuture != null) {
            linearFuture.cancel(false);
            linearFuture = null;
        }
    }

    /** 从池中随机挑选 count 个元素并随机打乱顺序 */
    private int[] randomPickAndShuffle(int[] pool, int count) {
        int[] copy = pool.clone();
        for (int i = copy.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = copy[i]; copy[i] = copy[j]; copy[j] = t;
        }
        if (count >= copy.length) return copy;
        int[] out = new int[count];
        System.arraycopy(copy, 0, out, 0, count);
        for (int i = out.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = out[i]; out[i] = out[j]; out[j] = t;
        }
        return out;
    }

    private void sendHeartbeat(ChannelHandlerContext ctx, String tag) {
        byte[] hb = HeartbeatCodec.encode(onlyCode);
        ctx.writeAndFlush(Unpooled.wrappedBuffer(hb));
        // System.out.println(onlyCode + " >>> " + tag + " HEX=" + Hex.toHex(hb));
    }

    private void sendHex(String hex, String tag) {
        if (channel == null || !channel.isActive()) {
            System.out.println(onlyCode + " 未连接");
            return;
        }
        byte[] b = Hex.fromHex(hex);
        channel.writeAndFlush(Unpooled.wrappedBuffer(b));
        System.out.println(onlyCode + " >>> " + tag + " HEX=" + hex.toUpperCase());
    }

    public void setPmDeviceClient(DeviceClient pmDeviceClient) {
        this.pmDeviceClient = pmDeviceClient;
    }

    private void cancelHeartbeat() {
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(false);
            heartbeatFuture = null;
        }
    }
}
