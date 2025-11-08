package com.sinloingok.app.service;

import com.sinloingok.app.config.SBeanUtils;
import com.sinloingok.app.util.ns.HandlerServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator; // [新增]
import io.netty.channel.*;
import io.netty.channel.AdaptiveRecvByteBufAllocator; // [新增]
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel; // [新增]
import io.netty.handler.logging.LoggingHandler; // [新增]
import io.netty.handler.timeout.IdleStateHandler; // [新增]
import io.netty.handler.timeout.ReadTimeoutHandler; // [新增]
import io.netty.handler.timeout.WriteTimeoutHandler; // [新增]
import io.netty.handler.traffic.GlobalTrafficShapingHandler; // [新增]
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService; // [新增]
import java.util.concurrent.ScheduledThreadPoolExecutor; // [新增]
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit; // [新增]
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class NettyServerManager implements NettyServerManagerMBean, ApplicationListener<ApplicationReadyEvent> {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    private volatile boolean isRunning = false;
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    @Value("${netty.server.port:18080}")
    private int port;

    @Value("${netty.server.host:0.0.0.0}")
    private String host;

    @Value("${netty.server.boss-threads:1}")
    private int bossThreads;

    @Value("${netty.server.worker-threads:0}")
    private int workerThreads;

    @Value("${netty.server.so-backlog:512}") // [修改] 調大隊列
    private int soBacklog;

    @Value("${netty.server.so-keepalive:true}")
    private boolean soKeepAlive;

    // ======= 新增：可配置的超時/空閒/整形參數 =======

    @Value("${netty.server.idle.read-seconds:30}")     // [新增] 讀空閒秒數，防NAT清表
    private int idleReadSeconds;

    @Value("${netty.server.idle.write-seconds:10}")    // [新增] 寫空閒秒數，定時發心跳
    private int idleWriteSeconds;

    @Value("${netty.server.timeout.read-seconds:20}")  // [新增] 讀超時，鏈路卡死快失敗
    private int readTimeoutSeconds;

    @Value("${netty.server.timeout.write-seconds:10}") // [新增] 寫超時
    private int writeTimeoutSeconds;

    @Value("${netty.server.enable-traffic-shaping:false}") // [新增] 是否啟用全局流量整形
    private boolean enableTrafficShaping;

    @Value("${netty.server.traffic.write-bytes-per-sec:16777216}") // 16MB/s
    private long writeLimit;

    @Value("${netty.server.traffic.read-bytes-per-sec:16777216}")  // 16MB/s
    private long readLimit;

    private GlobalTrafficShapingHandler globalShaper; // [新增]

    public synchronized void startServer() throws InterruptedException {
        if (isRunning) {
            log.warn("Netty服务器已经在运行中，无需重复启动");
            return;
        }

        log.info("正在启动Netty服务器... 配置: host={}, port={}, bossThreads={}, workerThreads={}",
                host, port, bossThreads, workerThreads);

        ThreadFactory bossThreadFactory = new DefaultThreadFactory("netty-boss") {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = super.newThread(r);
                thread.setUncaughtExceptionHandler((t, cause) ->
                        log.error("Boss线程 {} 发生未捕获异常", t.getName(), cause));
                return thread;
            }
        };

        ThreadFactory workerThreadFactory = new DefaultThreadFactory("netty-worker") {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = super.newThread(r);
                thread.setUncaughtExceptionHandler((t, cause) ->
                        log.error("Worker线程 {} 发生未捕获异常", t.getName(), cause));
                return thread;
            }
        };

        bossGroup = new NioEventLoopGroup(bossThreads, bossThreadFactory);
        workerGroup = new NioEventLoopGroup(workerThreads, workerThreadFactory);

        // [新增] 可選：全局流量整形，平滑突發，降低抖動/丟包
        ScheduledExecutorService shaperScheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "netty-shaper");
            t.setDaemon(true);
            return t;
        });
        if (enableTrafficShaping) {
            globalShaper = new GlobalTrafficShapingHandler(shaperScheduler, writeLimit, readLimit, 1000);
        }

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(host, port))
                    .option(ChannelOption.SO_BACKLOG, soBacklog)
                    .option(ChannelOption.SO_REUSEADDR, true) // [新增] 快速重綁
                    .handler(new LoggingHandler(LogLevel.INFO)) // [新增] Boss 端日誌
                    .childOption(ChannelOption.SO_KEEPALIVE, soKeepAlive)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // [新增] 自適應接收緩衝：避免一次讀太大導致抖動
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR,
                            new AdaptiveRecvByteBufAllocator(512, 4096, 65536))
                    // [新增] 內存池分配器：更穩定的堆外/池化
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    // [新增] 反壓水位：避免寫入堆積引發 OOM
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                            new WriteBufferWaterMark(64 * 1024, 128 * 1024))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();

                            if (enableTrafficShaping && globalShaper != null) {
                                p.addLast("globalShaper", globalShaper); // [新增]
                            }

                            // [新增] 服務端 Pipeline 的通用保護：空閒/超時
                            p.addLast("idle", new IdleStateHandler(
                                    idleReadSeconds, idleWriteSeconds, 0, TimeUnit.SECONDS));
                            p.addLast("rto", new ReadTimeoutHandler(readTimeoutSeconds));   // [新增]
                            p.addLast("wto", new WriteTimeoutHandler(writeTimeoutSeconds)); // [新增]

                            // [新增] 可寫性變化時自動暫停/恢復讀取（反壓協調）
                            p.addLast("writability", new ChannelDuplexHandler() {
                                @Override
                                public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
                                    boolean writable = ctx.channel().isWritable();
                                    ctx.channel().config().setAutoRead(writable);
                                    super.channelWritabilityChanged(ctx);
                                }
                            });

                            // [保持] 你的連接統計 + 業務處理器
                            p.addLast(new ConnectionStatisticsHandler());
                            p.addLast(SBeanUtils.getBean(HandlerServer.class));
                        }
                    });

            channelFuture = bootstrap.bind().sync();
            log.info("Netty服务器启动成功，开始在 {}:{} 上进行监听", host, port);

            isRunning = true;

            channelFuture.channel().closeFuture().addListener(future -> {
                log.info("Netty服务器通道已关闭");
                isRunning = false;
            });

        } catch (Exception e) {
            log.error("启动Netty服务器失败", e);
            stopServer();
            throw e;
        }
    }

    public synchronized void stopServer() {
        if (!isRunning) {
            log.warn("Netty服务器未运行，无需停止");
            return;
        }

        log.info("正在停止Netty服务器...");
        isRunning = false;

        try {
            if (channelFuture != null) {
                channelFuture.channel().close().sync();
                log.info("Netty服务器通道已完全关闭");
            }
        } catch (InterruptedException e) {
            log.warn("在等待通道关闭时被中断", e);
            Thread.currentThread().interrupt();
        } finally {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
                log.info("BossGroup已开始优雅关闭");
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
                log.info("WorkerGroup已开始优雅关闭");
            }
            connectionCount.set(0);
            log.info("Netty服务器已停止，资源清理完成");
        }
    }

    @Override
    public synchronized void restartServer() {
        log.info("开始重启Netty服务器...");
        try {
            stopServer();
            if (bossGroup != null) bossGroup.terminationFuture().sync();
            if (workerGroup != null) workerGroup.terminationFuture().sync();
            log.info("Netty服务器线程组已完全终止，开始重新启动...");
            startServer();
            log.info("Netty服务器重启成功");
        } catch (InterruptedException e) {
            log.error("重启Netty服务器时被中断", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("重启Netty服务器时发生异常", e);
        }
    }

    private class ConnectionStatisticsHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            int count = connectionCount.incrementAndGet();
            log.debug("客户端连接: {}. 当前活跃连接数: {}", ctx.channel().remoteAddress(), count);
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            int count = connectionCount.decrementAndGet();
            log.debug("客户端断开: {}. 当前活跃连接数: {}", ctx.channel().remoteAddress(), count);
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("连接 {} 发生异常", ctx.channel().remoteAddress(), cause);
            ctx.close();
        }
    }

    @Override
    public boolean isServerRunning() { return isRunning; }
    @Override
    public int getConnectionCount() { return connectionCount.get(); }
    @Override
    public int getBossThreads() { return bossThreads; }
    @Override
    public int getWorkerThreads() { return workerThreads; }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            log.info("检测到应用启动完成，正在自动启动Netty服务器...");
            this.startServer();
        } catch (InterruptedException e) {
            log.error("随应用启动Netty服务器失败", e);
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void preDestroy() {
        log.info("Spring容器正在销毁，正在关闭Netty服务器...");
        this.stopServer();
    }

    @javax.annotation.PostConstruct
    public void registerMBean() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("com.sinloingok.app.service:type=NettyServerManager");
            if (!mbs.isRegistered(name)) {
                mbs.registerMBean(this, name);
                log.info("NettyServerManager MBean 注册成功");
            }
        } catch (Exception e) {
            log.warn("注册MBean失败，不影响主要功能", e);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
