package com.sinloingok.app.simulator;

import com.google.common.collect.Lists;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class NettyDeviceClient {

    private final String host;
    private final int port;
    private final String onlyCode;
    private volatile Channel channel;
    private final Random rnd = new Random();
    private final AtomicBoolean roundRunning = new AtomicBoolean(false);
    private ScheduledFuture<?> currentFuture;

    public NettyDeviceClient(String host, int port, String onlyCode) {
        this.host = host;
        this.port = port;
        this.onlyCode = onlyCode;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            channel = ch;
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<Object>() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    log.info("设备 {} 已连接 {}:{}", onlyCode, host, port);
                                    sendHeartbeat(ctx, "HB(ONLINE)");
                                    ctx.channel().eventLoop().scheduleAtFixedRate(() ->
                                                    sendHeartbeat(ctx, "HB(periodic)"),
                                            1000, 1000, TimeUnit.MILLISECONDS);
                                }

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                                    if (msg instanceof ByteBuf) {
                                        ByteBuf buf = (ByteBuf) msg;
                                        byte[] arr = new byte[buf.readableBytes()];
                                        buf.readBytes(arr);
                                        log.info("设备 {} 收到HEX: {}", onlyCode, toHex(arr));
                                    } else {
                                        log.info("设备 {} 收到对象: {}", onlyCode, msg);
                                    }
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    log.warn("设备 {} 异常: {}", onlyCode, cause.toString());
                                    stopRound();
                                    ctx.close();
                                }
                            });
                        }
                    });

            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            stopRound();
            group.shutdownGracefully();
        }
    }

    // ===== 控制台调用接口 =====

    /** 开始轮番执行：随机通道执行若干次 */
    public void startRound(int times) {
        if (channel == null || !channel.isActive()) {
            log.warn("设备 {} 未连接", onlyCode);
            return;
        }
        if (roundRunning.get()) {
            log.info("设备 {} 已在运行", onlyCode);
            return;
        }
        roundRunning.set(true);
        log.info("设备 {} 启动随机轮番，times={}", onlyCode, times <= 0 ? "无限" : times);
        runRandomFlow(times);
    }

    /** 停止轮番任务 */
    public void stopRound() {
        roundRunning.set(false);
        if (currentFuture != null) currentFuture.cancel(false);
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(Unpooled.wrappedBuffer(buildClose(1))); // 安全关1号，仅防止误触
        }
        log.info("设备 {} 已停止轮番", onlyCode);
    }

    public boolean isRoundRunning() { return roundRunning.get(); }
    public String getOnlyCode() { return onlyCode; }
    public Channel channel() { return channel; }

    // ===== 核心逻辑 =====

    private void runRandomFlow(int times) {
        final int[] channels = {1,2,4,7,8};
        final EventLoop loop = channel.eventLoop();

        Runnable task = new Runnable() {
            int remain = times;
            @Override
            public void run() {
                if (!roundRunning.get() || channel == null || !channel.isActive()) return;
                int ch = channels[rnd.nextInt(channels.length)];
                openCh(ch);
                int delayClose = 3 + rnd.nextInt(8);
                loop.schedule(() -> {
                    closeCh(ch);
                    if (!roundRunning.get()) return;
                    if (remain > 0 && --remain <= 0) {
                        stopRound();
                        return;
                    }
                    int nextDelay = 3 + rnd.nextInt(8);
                    loop.schedule(this, nextDelay, TimeUnit.SECONDS);
                }, delayClose, TimeUnit.SECONDS);
            }
        };
        currentFuture = loop.schedule(task, 0, TimeUnit.SECONDS);
    }

    private void openCh(int ch) {
        byte[] f = buildOpen(ch);
        channel.writeAndFlush(Unpooled.wrappedBuffer(f));
        log.info("设备 {} >>> 开 {} HEX={}", onlyCode, ch, toHex(f));
    }

    private void closeCh(int ch) {
        byte[] f = buildClose(ch);
        channel.writeAndFlush(Unpooled.wrappedBuffer(f));
        log.info("设备 {} >>> 关 {} HEX={}", onlyCode, ch, toHex(f));
    }

    private void sendHeartbeat(ChannelHandlerContext ctx, String tag) {
        byte[] hb = buildHeartbeat(onlyCode);
        ctx.writeAndFlush(Unpooled.wrappedBuffer(hb));
        log.info("设备 {} >>> {} HEX={}", onlyCode, tag, toHex(hb));
    }

    // ===== 编码工具 =====

    /** 心跳：a50012594a + onlyCode(ASCII) */
    private static byte[] buildHeartbeat(String onlyCode) {
        byte[] prefix = hex("a50012594a");
        byte[] ascii = onlyCode.getBytes(StandardCharsets.US_ASCII);
        byte[] out = new byte[prefix.length + ascii.length];
        System.arraycopy(prefix, 0, out, 0, prefix.length);
        System.arraycopy(ascii, 0, out, prefix.length, ascii.length);
        return out;
    }

    /** 开：begin=2^(n-1)，end=00 */
    private static byte[] buildOpen(int n) {
        int val = 1 << (n - 1);
        return buildRaw(val & 0xFF, 0x00);
    }

    /** 关：begin=00，end=2^(n-1) */
    private static byte[] buildClose(int n) {
        int val = 1 << (n - 1);
        return buildRaw(0x00, val & 0xFF);
    }

    /** 主体帧结构：eeffc001 + pad(8B) + [begin低][00][end低][00] */
    private static byte[] buildRaw(int beginLow, int endLow) {
        byte[] prefix = hex("eeffc001");
        byte[] pad8 = new byte[]{0x01,0x00,0x00,0x01,0x00,0x00,0x00,0x00};
        byte[] body = new byte[]{(byte) beginLow, 0x00, (byte) endLow, 0x00};
        byte[] out = new byte[prefix.length + pad8.length + body.length];
        System.arraycopy(prefix, 0, out, 0, prefix.length);
        System.arraycopy(pad8, 0, out, prefix.length, pad8.length);
        System.arraycopy(body, 0, out, prefix.length + pad8.length, body.length);
        return out;
    }

    private static byte[] hex(String s) {
        int len = s.length() / 2;
        byte[] out = new byte[len];
        for (int i = 0; i < len; i++)
            out[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        return out;
    }

    private static String toHex(byte[] data) {
        char[] HEX = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) sb.append(HEX[(b >> 4) & 0xF]).append(HEX[b & 0xF]);
        return sb.toString();
    }

    // ===== 启动多设备 + 控制台交互 =====

    public static void main(String[] args) {
        List<String> list = Lists.newArrayList(
                "0090D500245B",
                "0090D5000F10",
                "0090D500242A",
                "0090D5000F4A",
                "0090B2B6215D"
        );

        Map<String, NettyDeviceClient> map = new ConcurrentHashMap<>();
        for (String onlyCode : list) {
            new Thread(() -> {
                try {
                    NettyDeviceClient c = new NettyDeviceClient("127.0.0.1", 18080, onlyCode);
                    map.put(onlyCode, c);
                    c.start();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, "dev-" + onlyCode).start();
        }

        // 控制台命令
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println(" 命令:\n" +
                    "                  open <onlyCode> <ch>    —— 打开通道\n" +
                    "                  close <onlyCode> <ch>   —— 关闭通道\n" +
                    "                  start <onlyCode> [n]    —— 启动随机轮番（n次或无限）\n" +
                    "                  stop <onlyCode>         —— 停止轮番\n" +
                    "                  list                    —— 查看设备状态\n" +
                    "                  quit                    —— 退出");
            while (true) {
                System.out.print("> ");
                if (!sc.hasNextLine()) break;
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] t = line.split("\\s+");
                String cmd = t[0].toLowerCase(Locale.ROOT);
                switch (cmd) {
                    case "quit" : System.exit(0);break;
                    case "list" : map.forEach((k,v) ->
                            System.out.println(k + " : " +
                                    ((v.channel()!=null && v.channel().isActive())?"ACTIVE":"INACTIVE") +
                                    ", round=" + v.isRoundRunning()));break;
                    case "open" : {
                        if (t.length < 3) { System.out.println("用法: open <onlyCode> <ch>"); break; }
                        NettyDeviceClient c = map.get(t[1]);
                        if (c==null) { System.out.println("未知 onlyCode"); break; }
                        int ch = Integer.parseInt(t[2]);
                        c.openCh(ch);break;
                    }
                    case "close" : {
                        if (t.length < 3) { System.out.println("用法: close <onlyCode> <ch>"); break; }
                        NettyDeviceClient c = map.get(t[1]);
                        if (c==null) { System.out.println("未知 onlyCode"); break; }
                        int ch = Integer.parseInt(t[2]);
                        c.closeCh(ch);break;
                    }
                    case "start" : {
                        if (t.length < 2) { System.out.println("用法: start <onlyCode> [n]"); break; }
                        NettyDeviceClient c = map.get(t[1]);
                        if (c==null) { System.out.println("未知 onlyCode"); break; }
                        int times = (t.length>=3)?Integer.parseInt(t[2]):0;
                        c.startRound(times);break;
                    }
                    case "stop" : {
                        if (t.length < 2) { System.out.println("用法: stop <onlyCode>"); break; }
                        NettyDeviceClient c = map.get(t[1]);
                        if (c==null) { System.out.println("未知 onlyCode"); break; }
                        c.stopRound();break;
                    }
                    default : System.out.println("未知命令: " + cmd);
                }
            }
        }
    }
}
