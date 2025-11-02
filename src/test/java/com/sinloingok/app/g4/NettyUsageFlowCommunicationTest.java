//package com.sinloingok.app.g4;
//
//import com.sinloingok.app.models.DeviceControl;
//import com.sinloingok.app.service.netsite.NetSiteService;
//import com.sinloingok.app.service.order.CommandExecutor;
//import com.sinloingok.app.service.order.DeviceCommandSender;
//import com.sinloingok.app.service.order.SettlementService;
//import com.sinloingok.app.simulator.SwitchCodec;
//import com.sinloingok.app.util.ns.HandlerServer;
//import com.sinloingok.app.util.ns.HeartbeatProcessor;
//import com.sinloingok.app.util.ns.NettyChannelRegistry;
//import com.sinloingok.app.util.ns.PulseSignalProcessor;
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.ByteBufUtil;
//import io.netty.channel.*;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.lang.reflect.Field;
//import java.net.InetSocketAddress;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//import java.util.Objects;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.junit.Assert.*;
//
///**
// * 使用實際的 {@link HandlerServer} 與 {@link NettyDeviceClient} 聯調，
// * 驗證正向、逆向與自動切換等多種業務流程。
// */
//public class NettyUsageFlowCommunicationTest {
//
//    private static final String ONLY_CODE = "0090D500245B";
//
//    private HandlerHarness harness;
//    private Thread clientThread;
//
//    @Before
//    public void setUp() throws Exception {
//        FlowScenario scenario = FlowScenario.defaultScenario();
//        harness = new HandlerHarness(scenario);
//        harness.start();
//
//        int port = harness.getBoundPort();
//        clientThread = new Thread(() -> {
//            NettyDeviceClient client = new NettyDeviceClient("127.0.0.1", port, ONLY_CODE);
//            try {
//                client.start();
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }, "mock-device-thread");
//        clientThread.start();
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        if (clientThread != null) {
//            clientThread.join(TimeUnit.SECONDS.toMillis(5));
//            if (clientThread.isAlive()) {
//                clientThread.interrupt();
//            }
//        }
//        if (harness != null) {
//            harness.stop();
//        }
//    }
//
//    @Test
//    public void simulateForwardReverseAndAutoToggleFlows() throws Exception {
//        assertTrue("HandlerServer 應在 30 秒內完成整套指令腳本", harness.awaitScenario(30, TimeUnit.SECONDS));
//        assertTrue("每一條指令都應收到設備回覆的 4F4B21", harness.getScenario().awaitAcks(30, TimeUnit.SECONDS));
//        assertEquals("指令回執數量必須與腳本步驟一致", harness.getScenario().getExpectedAckTotal(), harness.getScenario().getAckCount());
//        assertFalse("設備模擬執行完腳本後應結束", clientThread.isAlive());
//    }
//
//    /** 描述單步指令（含中文註釋與延遲）。 */
//    private static final class ScenarioStep {
//        private final String description;
//        private final String hexCommand;
//        private final long delayAfterMillis;
//
//        ScenarioStep(String description, String hexCommand, long delayAfterMillis) {
//            this.description = Objects.requireNonNull(description, "description");
//            this.hexCommand = Objects.requireNonNull(hexCommand, "hexCommand");
//            this.delayAfterMillis = delayAfterMillis;
//        }
//    }
//
//    /**
//     * 劇本：整理所有需要測試的流程，並追蹤 4F4B21 回覆數量。
//     * 包含：
//     *  - 自動切換通道（僅 OPEN 兩次）——覆蓋 case 2/4/5/7/8。
//     *  - 正向流程：QS1 開啟 → QS2 多次開關 → 其它服務 → 結算。
//     *  - 逆向流程：QS1 關閉狀態先操作 → 再補開 QS1 → 結算。
//     */
//    private static final class FlowScenario {
//        private final List<ScenarioStep> steps;
//        private final CountDownLatch ackLatch;
//        private final AtomicInteger ackCount = new AtomicInteger();
//
//        private FlowScenario(List<ScenarioStep> steps) {
//            this.steps = steps;
//            this.ackLatch = new CountDownLatch(steps.size());
//        }
//
//        static FlowScenario defaultScenario() {
//            List<ScenarioStep> steps = new ArrayList<>();
//
//            // 自動切換：通道 2/4/5/7/8 僅發送 OPEN 兩次。
//            steps.add(step("自動切換：通道2第一次 OPEN", SwitchCodec.buildOpen(2)));
//            steps.add(step("自動切換：通道2第二次 OPEN", SwitchCodec.buildOpen(2)));
//            steps.add(step("自動切換：通道4第一次 OPEN", SwitchCodec.buildOpen(4)));
//            steps.add(step("自動切換：通道4第二次 OPEN", SwitchCodec.buildOpen(4)));
//            steps.add(step("自動切換：通道5第一次 OPEN", SwitchCodec.buildOpen(5)));
//            steps.add(step("自動切換：通道5第二次 OPEN", SwitchCodec.buildOpen(5)));
//            steps.add(step("自動切換：通道7第一次 OPEN", SwitchCodec.buildOpen(7)));
//            steps.add(step("自動切換：通道7第二次 OPEN", SwitchCodec.buildOpen(7)));
//            steps.add(step("自動切換：QS1(通道8)第一次 OPEN", SwitchCodec.buildOpen(8)));
//            steps.add(step("自動切換：QS1(通道8)第二次 OPEN", SwitchCodec.buildOpen(8)));
//
//            // 正向流程。
//            steps.add(step("正向流程：QS1 開啟後首次 QS2 OPEN", SwitchCodec.buildOpen(3)));
//            steps.add(step("正向流程：QS1 開啟後 QS2 CLOSE", SwitchCodec.buildClose(3)));
//            steps.add(step("正向流程：QS1 開啟後再次 QS2 OPEN", SwitchCodec.buildOpen(3)));
//            steps.add(step("正向流程：關閉 QS1 結束清水", SwitchCodec.buildClose(8)));
//            steps.add(step("正向流程：QS1 關閉後仍嘗試 QS2 OPEN", SwitchCodec.buildOpen(3)));
//            steps.add(step("正向流程：QS1 關閉後仍嘗試 QS2 CLOSE", SwitchCodec.buildClose(3)));
//            steps.add(step("正向流程：吸塵服務 OPEN", SwitchCodec.buildOpen(7)));
//            steps.add(step("正向流程：吸塵服務 CLOSE", SwitchCodec.buildClose(7)));
//            steps.add(step("正向流程：洗手服務 OPEN", SwitchCodec.buildOpen(2)));
//            steps.add(step("正向流程：洗手服務 CLOSE", SwitchCodec.buildClose(2)));
//            steps.add(step("正向流程：鍍膜服務 OPEN", SwitchCodec.buildOpen(5)));
//            steps.add(step("正向流程：鍍膜服務 CLOSE", SwitchCodec.buildClose(5)));
//            steps.add(step("正向流程：吹氣服務 OPEN", SwitchCodec.buildOpen(4)));
//            steps.add(step("正向流程：吹氣服務 CLOSE", SwitchCodec.buildClose(4)));
//            steps.add(step("正向流程：結算關機指令", SwitchCodec.buildClose(6)));
//
//            // 逆向流程。
//            steps.add(step("逆向流程：QS1 未開先嘗試 QS2 OPEN", SwitchCodec.buildOpen(3)));
//            steps.add(step("逆向流程：QS1 未開先嘗試 QS2 CLOSE", SwitchCodec.buildClose(3)));
//            steps.add(step("逆向流程：先啟動吸塵 OPEN", SwitchCodec.buildOpen(7)));
//            steps.add(step("逆向流程：結束吸塵 CLOSE", SwitchCodec.buildClose(7)));
//            steps.add(step("逆向流程：先啟動洗手 OPEN", SwitchCodec.buildOpen(2)));
//            steps.add(step("逆向流程：結束洗手 CLOSE", SwitchCodec.buildClose(2)));
//            steps.add(step("逆向流程：先啟動鍍膜 OPEN", SwitchCodec.buildOpen(5)));
//            steps.add(step("逆向流程：結束鍍膜 CLOSE", SwitchCodec.buildClose(5)));
//            steps.add(step("逆向流程：補開 QS1", SwitchCodec.buildOpen(8)));
//            steps.add(step("逆向流程：QS1 開啟後 QS2 OPEN", SwitchCodec.buildOpen(3)));
//            steps.add(step("逆向流程：QS1 開啟後 QS2 CLOSE", SwitchCodec.buildClose(3)));
//            steps.add(step("逆向流程：再次閉 QS1", SwitchCodec.buildClose(8)));
//            steps.add(step("逆向流程：QS1 關閉後再嘗試 QS2 OPEN", SwitchCodec.buildOpen(3)));
//            steps.add(step("逆向流程：QS1 關閉後再嘗試 QS2 CLOSE", SwitchCodec.buildClose(3)));
//            steps.add(step("逆向流程：結算指令", SwitchCodec.buildClose(6)));
//
//            return new FlowScenario(steps);
//        }
//
//        private static ScenarioStep step(String description, String hex) {
//            return new ScenarioStep(description, hex.toUpperCase(Locale.ROOT), 150);
//        }
//
//        List<ScenarioStep> getSteps() {
//            return steps;
//        }
//
//        void onAck() {
//            int current = ackCount.incrementAndGet();
//            if (current <= steps.size()) {
//                ackLatch.countDown();
//            }
//        }
//
//        int getExpectedAckTotal() {
//            return steps.size();
//        }
//
//        int getAckCount() {
//            return ackCount.get();
//        }
//
//        boolean awaitAcks(long timeout, TimeUnit unit) throws InterruptedException {
//            return ackLatch.await(timeout, unit);
//        }
//    }
//
//    /** 封裝基於 HandlerServer 的臨時 Netty 服務端。 */
//    private static final class HandlerHarness {
//        private final FlowScenario scenario;
//        private final NettyChannelRegistry channelRegistry = new NettyChannelRegistry();
//        private final HandlerServer handlerServer;
//        private final ScenarioOrchestrator orchestrator;
//        private final ScenarioCoordinator coordinator;
//
//        private EventLoopGroup bossGroup;
//        private EventLoopGroup workerGroup;
//        private ChannelFuture bindFuture;
//
//        HandlerHarness(FlowScenario scenario) {
//            this.scenario = scenario;
//            HeartbeatProcessor heartbeatProcessor = new HeartbeatProcessor(channelRegistry);
//            PulseSignalProcessor pulseSignalProcessor = new PulseSignalProcessor(channelRegistry);
//            this.handlerServer = new HandlerServer(channelRegistry, heartbeatProcessor, pulseSignalProcessor);
//            injectDependencies(handlerServer, channelRegistry);
//            this.orchestrator = new ScenarioOrchestrator(channelRegistry, scenario);
//            this.coordinator = new ScenarioCoordinator(channelRegistry, scenario, orchestrator);
//        }
//
//        void start() throws InterruptedException {
//            bossGroup = new NioEventLoopGroup(1);
//            workerGroup = new NioEventLoopGroup(1);
//
//            ServerBootstrap bootstrap = new ServerBootstrap();
//            bootstrap.group(bossGroup, workerGroup)
//                    .channel(NioServerSocketChannel.class)
//                    .childHandler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        protected void initChannel(SocketChannel ch) {
//                            ch.pipeline().addLast(coordinator);
//                            ch.pipeline().addLast(handlerServer);
//                        }
//                    });
//
//            bindFuture = bootstrap.bind(0).sync();
//        }
//
//        int getBoundPort() {
//            return ((InetSocketAddress) bindFuture.channel().localAddress()).getPort();
//        }
//
//        boolean awaitScenario(long timeout, TimeUnit unit) throws InterruptedException {
//            return orchestrator.awaitCompletion(timeout, unit);
//        }
//
//        FlowScenario getScenario() {
//            return scenario;
//        }
//
//        void stop() throws InterruptedException {
//            try {
//                if (bindFuture != null) {
//                    bindFuture.channel().close().sync();
//                }
//            } finally {
//                if (workerGroup != null) {
//                    workerGroup.shutdownGracefully().sync();
//                }
//                if (bossGroup != null) {
//                    bossGroup.shutdownGracefully().sync();
//                }
//            }
//        }
//
//        private static void injectDependencies(HandlerServer handlerServer, NettyChannelRegistry registry) {
//            try {
//                Field netSiteField = HandlerServer.class.getDeclaredField("netSiteService");
//                netSiteField.setAccessible(true);
//                netSiteField.set(handlerServer, new NoopNetSiteService());
//
//                DeviceCommandSender sender = new DeviceCommandSender(registry);
//                NoopSettlementService settlement = new NoopSettlementService();
//                NoopCommandExecutor executor = new NoopCommandExecutor(sender, settlement);
//
//                Field executorField = HandlerServer.class.getDeclaredField("executor");
//                executorField.setAccessible(true);
//                executorField.set(handlerServer, executor);
//            } catch (ReflectiveOperationException e) {
//                throw new IllegalStateException("無法注入 HandlerServer 依賴", e);
//            }
//        }
//    }
//
//    /**
//     * 監聽心跳/回執並與劇本協調。
//     */
//    @ChannelHandler.Sharable
//    private static final class ScenarioCoordinator extends ChannelInboundHandlerAdapter {
//        private static final String HEARTBEAT_PREFIX = "A50012594A";
//        private static final String ACK_HEX = "4F4B21";
//
//        private final NettyChannelRegistry channelRegistry;
//        private final FlowScenario scenario;
//        private final ScenarioOrchestrator orchestrator;
//
//        private ScenarioCoordinator(NettyChannelRegistry channelRegistry, FlowScenario scenario, ScenarioOrchestrator orchestrator) {
//            this.channelRegistry = channelRegistry;
//            this.scenario = scenario;
//            this.orchestrator = orchestrator;
//        }
//
//        @Override
//        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            if (msg instanceof ByteBuf) {
//                ByteBuf buf = (ByteBuf) msg;
//                String hex = ByteBufUtil.hexDump(buf).toUpperCase(Locale.ROOT);
//                if (hex.startsWith(HEARTBEAT_PREFIX)) {
//                    orchestrator.trigger(ctx.channel());
//                } else if (ACK_HEX.equals(hex)) {
//                    scenario.onAck();
//                    System.out.println("[SERVER] 收到設備回覆4F4B21, 已確認=" + scenario.getAckCount() + "/" + scenario.getExpectedAckTotal());
//                }
//            }
//            super.channelRead(ctx, msg);
//        }
//
//        @Override
//        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//            ctx.close();
//            super.exceptionCaught(ctx, cause);
//        }
//    }
//
//    /**
//     * 根據 HandlerServer 註冊的 channel，依序下發劇本指令。
//     */
//    private static final class ScenarioOrchestrator {
//        private final NettyChannelRegistry registry;
//        private final FlowScenario scenario;
//        private final CountDownLatch completionLatch = new CountDownLatch(1);
//        private final AtomicBoolean started = new AtomicBoolean(false);
//
//        private ScenarioOrchestrator(NettyChannelRegistry registry, FlowScenario scenario) {
//            this.registry = registry;
//            this.scenario = scenario;
//        }
//
//        void trigger(Channel channel) {
//            if (started.get()) {
//                return;
//            }
//            channel.eventLoop().execute(() -> attemptStart(channel));
//        }
//
//        private void attemptStart(Channel channel) {
//            if (started.get()) {
//                return;
//            }
//            String onlyCode = registry.getOnlyCode(channel);
//            if (onlyCode == null) {
//                channel.eventLoop().schedule(() -> attemptStart(channel), 50, TimeUnit.MILLISECONDS);
//                return;
//            }
//            if (started.compareAndSet(false, true)) {
//                sendStep(channel, onlyCode, 0);
//            }
//        }
//
//        private void sendStep(Channel channel, String onlyCode, int index) {
//            if (index >= scenario.getSteps().size()) {
//                channel.eventLoop().schedule(() -> {
//                    completionLatch.countDown();
//                    channel.close();
//                }, 300, TimeUnit.MILLISECONDS);
//                return;
//            }
//
//            ScenarioStep step = scenario.getSteps().get(index);
//            System.out.println("[SERVER] 下發指令(" + (index + 1) + "/" + scenario.getSteps().size() + ") -> " + step.description + " HEX=" + step.hexCommand);
//            registry.sendMsg(onlyCode, step.hexCommand);
//
//            channel.eventLoop().schedule(() -> sendStep(channel, onlyCode, index + 1), step.delayAfterMillis, TimeUnit.MILLISECONDS);
//        }
//
//        boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
//            return completionLatch.await(timeout, unit);
//        }
//    }
//
//    private static final class NoopNetSiteService extends NetSiteService {
//        @Override
//        public void registerOrRefreshNetSite(String onlyCode) {
//            // 測試環境下不需要實際刷新站點狀態
//        }
//    }
//
//    private static final class NoopSettlementService extends SettlementService {
//        @Override
//        public void settleChannel(String onlyCode, int channel, DeviceControl.Action command) {
//            // 測試環境下跳過結算
//        }
//
//        @Override
//        public void settleAll(String onlyCode) {
//            // 測試環境下跳過結算
//        }
//    }
//
//    private static final class NoopCommandExecutor extends CommandExecutor {
//        NoopCommandExecutor(DeviceCommandSender sender, SettlementService settlementService) {
//            super(sender, settlementService);
//        }
//
//        @Override
//        public boolean executeWithTrace(String onlyCode, int chNum, DeviceControl.Action commandStr, String traceId) {
//            return true;
//        }
//
//        @Override
//        public boolean execute(String onlyCode, int chNum, DeviceControl.Action commandStr) {
//            return true;
//        }
//    }
//}
