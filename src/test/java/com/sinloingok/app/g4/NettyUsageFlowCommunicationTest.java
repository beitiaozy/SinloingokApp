package com.sinloingok.app.g4;

import com.sinloingok.app.simulator.SwitchCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 集成測試：使用 Netty 模擬設備與服務端之間的真實通信流程，覆蓋正向、逆向及多輪使用場景。
 * 測試用例會啟動一個臨時 Netty 服務端，並驅動 {@link NettyDeviceClient} 發送心跳、回傳 4F4B21 確認碼。
 */
public class NettyUsageFlowCommunicationTest {

    private static final String ONLY_CODE = "0090D500245B";

    private FlowTestServer server;
    private Thread clientThread;

    @Before
    public void setUp() throws Exception {
        FlowScenario scenario = FlowScenario.defaultScenario();
        server = new FlowTestServer(0, scenario);
        server.start();

        int port = server.getBoundPort();
        clientThread = new Thread(() -> {
            NettyDeviceClient client = new NettyDeviceClient("127.0.0.1", port, ONLY_CODE);
            try {
                client.start();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "mock-device-thread");
        clientThread.start();
    }

    @After
    public void tearDown() throws Exception {
        if (clientThread != null) {
            clientThread.join(TimeUnit.SECONDS.toMillis(5));
            if (clientThread.isAlive()) {
                clientThread.interrupt();
            }
        }
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void simulateForwardReverseAndAutoToggleFlows() throws Exception {
        assertTrue("服務端應在 30 秒內完成整套指令腳本", server.awaitScenario(30, TimeUnit.SECONDS));
        assertTrue("每一條指令都應收到設備回覆的 4F4B21", server.getScenario().awaitAcks(30, TimeUnit.SECONDS));
        assertEquals("指令回執數量必須與腳本步驟一致", server.getScenario().getExpectedAckTotal(), server.getScenario().getAckCount());
        assertFalse("設備模擬執行完腳本後應結束", clientThread.isAlive());
    }

    /** 單步指令（含中文註釋與延遲），描述服務端要下發的操作。 */
    private static final class ScenarioStep {
        private final String description;
        private final String hexCommand;
        private final long delayAfterMillis;

        ScenarioStep(String description, String hexCommand, long delayAfterMillis) {
            this.description = Objects.requireNonNull(description, "description");
            this.hexCommand = Objects.requireNonNull(hexCommand, "hexCommand");
            this.delayAfterMillis = delayAfterMillis;
        }
    }

    /**
     * 試劇本：整理所有需要測試的流程，並追蹤 4F4B21 回覆數量。
     * 包含：
     *  - 自動切換通道（僅 OPEN 兩次）——覆蓋 case 2/4/5/7/8。
     *  - 正向流程：QS1 開啟 → QS2 多次開關 → 其它服務 → 結算。
     *  - 逆向流程：QS1 關閉狀態先操作 → 再補開 QS1 → 結算。
     */
    private static final class FlowScenario {
        private final List<ScenarioStep> steps;
        private final CountDownLatch ackLatch;
        private final AtomicInteger ackCount = new AtomicInteger();

        private FlowScenario(List<ScenarioStep> steps) {
            this.steps = steps;
            this.ackLatch = new CountDownLatch(steps.size());
        }

        static FlowScenario defaultScenario() {
            List<ScenarioStep> steps = new ArrayList<>();

            // 自動切換通道：僅發送 OPEN 兩次，模擬 case 2/4/5/7/8 的行為。
            steps.add(step("自動切換：通道2第一次 OPEN", SwitchCodec.buildOpen(2)));
            steps.add(step("自動切換：通道2第二次 OPEN", SwitchCodec.buildOpen(2)));
            steps.add(step("自動切換：通道4第一次 OPEN", SwitchCodec.buildOpen(4)));
            steps.add(step("自動切換：通道4第二次 OPEN", SwitchCodec.buildOpen(4)));
            steps.add(step("自動切換：通道5第一次 OPEN", SwitchCodec.buildOpen(5)));
            steps.add(step("自動切換：通道5第二次 OPEN", SwitchCodec.buildOpen(5)));
            steps.add(step("自動切換：通道7第一次 OPEN", SwitchCodec.buildOpen(7)));
            steps.add(step("自動切換：通道7第二次 OPEN", SwitchCodec.buildOpen(7)));
            steps.add(step("自動切換：QS1(通道8)第一次 OPEN", SwitchCodec.buildOpen(8)));
            steps.add(step("自動切換：QS1(通道8)第二次 OPEN", SwitchCodec.buildOpen(8)));

            // 正向流程：QS1 開 → QS2 多次 → 關 → 其它服務 → 結算。
            steps.add(step("正向流程：QS1 開啟後首次 QS2 OPEN", SwitchCodec.buildOpen(3)));
            steps.add(step("正向流程：QS1 開啟後 QS2 CLOSE", SwitchCodec.buildClose(3)));
            steps.add(step("正向流程：QS1 開啟後再次 QS2 OPEN", SwitchCodec.buildOpen(3)));
            steps.add(step("正向流程：關閉 QS1 結束清水", SwitchCodec.buildClose(8)));
            steps.add(step("正向流程：QS1 關閉後仍嘗試 QS2 OPEN", SwitchCodec.buildOpen(3)));
            steps.add(step("正向流程：QS1 關閉後仍嘗試 QS2 CLOSE", SwitchCodec.buildClose(3)));
            steps.add(step("正向流程：吸塵服務 OPEN", SwitchCodec.buildOpen(7)));
            steps.add(step("正向流程：吸塵服務 CLOSE", SwitchCodec.buildClose(7)));
            steps.add(step("正向流程：洗手服務 OPEN", SwitchCodec.buildOpen(2)));
            steps.add(step("正向流程：洗手服務 CLOSE", SwitchCodec.buildClose(2)));
            steps.add(step("正向流程：鍍膜服務 OPEN", SwitchCodec.buildOpen(5)));
            steps.add(step("正向流程：鍍膜服務 CLOSE", SwitchCodec.buildClose(5)));
            steps.add(step("正向流程：吹氣服務 OPEN", SwitchCodec.buildOpen(4)));
            steps.add(step("正向流程：吹氣服務 CLOSE", SwitchCodec.buildClose(4)));
            steps.add(step("正向流程：結算關機指令", SwitchCodec.buildClose(6)));

            // 逆向流程：未開 QS1 先操作，再補開與結算。
            steps.add(step("逆向流程：QS1 未開先嘗試 QS2 OPEN", SwitchCodec.buildOpen(3)));
            steps.add(step("逆向流程：QS1 未開先嘗試 QS2 CLOSE", SwitchCodec.buildClose(3)));
            steps.add(step("逆向流程：先啟動吸塵 OPEN", SwitchCodec.buildOpen(7)));
            steps.add(step("逆向流程：結束吸塵 CLOSE", SwitchCodec.buildClose(7)));
            steps.add(step("逆向流程：先啟動洗手 OPEN", SwitchCodec.buildOpen(2)));
            steps.add(step("逆向流程：結束洗手 CLOSE", SwitchCodec.buildClose(2)));
            steps.add(step("逆向流程：先啟動鍍膜 OPEN", SwitchCodec.buildOpen(5)));
            steps.add(step("逆向流程：結束鍍膜 CLOSE", SwitchCodec.buildClose(5)));
            steps.add(step("逆向流程：補開 QS1", SwitchCodec.buildOpen(8)));
            steps.add(step("逆向流程：QS1 開啟後 QS2 OPEN", SwitchCodec.buildOpen(3)));
            steps.add(step("逆向流程：QS1 開啟後 QS2 CLOSE", SwitchCodec.buildClose(3)));
            steps.add(step("逆向流程：再次關閉 QS1", SwitchCodec.buildClose(8)));
            steps.add(step("逆向流程：QS1 關閉後再嘗試 QS2 OPEN", SwitchCodec.buildOpen(3)));
            steps.add(step("逆向流程：QS1 關閉後再嘗試 QS2 CLOSE", SwitchCodec.buildClose(3)));
            steps.add(step("逆向流程：結算指令", SwitchCodec.buildClose(6)));

            return new FlowScenario(steps);
        }

        private static ScenarioStep step(String description, String hex) {
            return new ScenarioStep(description, hex.toUpperCase(Locale.ROOT), 150);
        }

        List<ScenarioStep> getSteps() {
            return steps;
        }

        void onAck() {
            ackCount.incrementAndGet();
            ackLatch.countDown();
        }

        int getExpectedAckTotal() {
            return steps.size();
        }

        int getAckCount() {
            return ackCount.get();
        }

        boolean awaitAcks(long timeout, TimeUnit unit) throws InterruptedException {
            return ackLatch.await(timeout, unit);
        }
    }

    /** 針對測試啟動一個最小 Netty 服務端，負責按照劇本下發指令。 */
    private static final class FlowTestServer {
        private final int port;
        private final FlowScenario scenario;
        private final FlowServerHandler handler;

        private EventLoopGroup bossGroup;
        private EventLoopGroup workerGroup;
        private ChannelFuture bindFuture;

        FlowTestServer(int port, FlowScenario scenario) {
            this.port = port;
            this.scenario = scenario;
            this.handler = new FlowServerHandler(scenario);
        }

        void start() throws InterruptedException {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(1);

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(handler);
                        }
                    });

            bindFuture = bootstrap.bind(port).sync();
        }

        int getBoundPort() {
            return ((InetSocketAddress) bindFuture.channel().localAddress()).getPort();
        }

        boolean awaitScenario(long timeout, TimeUnit unit) throws InterruptedException {
            return handler.awaitScenario(timeout, unit);
        }

        FlowScenario getScenario() {
            return scenario;
        }

        void stop() throws InterruptedException {
            try {
                if (bindFuture != null) {
                    bindFuture.channel().close().sync();
                }
            } finally {
                if (workerGroup != null) {
                    workerGroup.shutdownGracefully().sync();
                }
                if (bossGroup != null) {
                    bossGroup.shutdownGracefully().sync();
                }
            }
        }
    }

    /** 真實服務端的簡化 Handler：接收心跳、下發命令並等待 4F4B21 回覆。 */
    @ChannelHandler.Sharable
    private static final class FlowServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private static final String HEARTBEAT_PREFIX = "A50012594A";
        private static final String ACK_HEX = "4F4B21";

        private final FlowScenario scenario;
        private final CountDownLatch scenarioLatch = new CountDownLatch(1);
        private final AtomicBoolean started = new AtomicBoolean(false);
        private final AtomicBoolean handshakeAck = new AtomicBoolean(false);

        private FlowServerHandler(FlowScenario scenario) {
            this.scenario = scenario;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            // 連接建立時不立即發送指令，等待心跳識別設備。
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
            String hex = toHex(msg);

            if (hex.startsWith(HEARTBEAT_PREFIX)) {
                // 解析 onlyCode，僅供日誌觀察。
                String onlyCode = new String(fromHex(hex.substring(HEARTBEAT_PREFIX.length())), StandardCharsets.US_ASCII);
                if (started.compareAndSet(false, true)) {
                    ctx.executor().execute(() -> runScenario(ctx));
                }
                ctx.writeAndFlush(Unpooled.copiedBuffer("PONG", CharsetUtil.US_ASCII));
                System.out.println("[SERVER] 收到心跳，設備=" + onlyCode + "，返回 PONG");
            } else if (ACK_HEX.equalsIgnoreCase(hex)) {
                if (handshakeAck.compareAndSet(false, true)) {
                    System.out.println("[SERVER] 握手確認 4F4B21, 開始追蹤指令回執");
                    return;
                }
                scenario.onAck();
                System.out.println("[SERVER] 收到設備回覆4F4B21, 已確認=" + scenario.getAckCount() + "/" + scenario.getExpectedAckTotal());
            } else {
                System.out.println("[SERVER] 收到未知HEX=" + hex);
            }
        }

        private void runScenario(ChannelHandlerContext ctx) {
            List<ScenarioStep> steps = scenario.getSteps();
            if (steps.isEmpty()) {
                scenarioLatch.countDown();
                ctx.close();
                return;
            }

            sendStep(ctx, steps, 0);
        }

        private void sendStep(ChannelHandlerContext ctx, List<ScenarioStep> steps, int index) {
            if (index >= steps.size()) {
                ctx.executor().schedule(() -> {
                    scenarioLatch.countDown();
                    ctx.close();
                }, 300, TimeUnit.MILLISECONDS);
                return;
            }

            ScenarioStep step = steps.get(index);
            System.out.println("[SERVER] 下發指令(" + (index + 1) + "/" + steps.size() + ") -> " + step.description + " HEX=" + step.hexCommand);
            ctx.writeAndFlush(Unpooled.wrappedBuffer(fromHex(step.hexCommand)));

            ctx.executor().schedule(() -> sendStep(ctx, steps, index + 1), step.delayAfterMillis, TimeUnit.MILLISECONDS);
        }

        boolean awaitScenario(long timeout, TimeUnit unit) throws InterruptedException {
            return scenarioLatch.await(timeout, unit);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            scenarioLatch.countDown();
            ctx.close();
        }
    }

    private static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return out;
    }

    private static String toHex(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(Locale.ROOT, "%02X", b));
        }
        return sb.toString();
    }
}

