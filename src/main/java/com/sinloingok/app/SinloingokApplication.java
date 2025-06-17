package com.sinloingok.app;

import com.sinloingok.app.service.NettyServerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**
 * Application entry point for the Sinloingok Spring Boot application.
 */
@Slf4j
@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = {"com.sinloingok.app.*", "com.sinloingok.app.scheduled"})
public class SinloingokApplication {

    /**
     * Launches the Spring Boot application.
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SinloingokApplication.class, args);
        Environment env = context.getEnvironment();
        // 打印关键信息
        log.info("\n=== Spring Boot启动成功 ===");
        log.info("运行环境: {}", env.getProperty("spring.profiles.active", "default"));
        log.info("端口: {}", env.getProperty("server.port"));
        log.info("数据库URL:{} ", env.getProperty("spring.datasource.url"));
    }

    /**
     * 应用启动后立即初始化Netty服务器
     */
    @Component
    public static class NettyServerInitializer {

        private final NettyServerManager nettyServerManager;

        public NettyServerInitializer(NettyServerManager nettyServerManager) {
            this.nettyServerManager = nettyServerManager;
        }

        @EventListener(ApplicationReadyEvent.class) // 监听应用启动完成事件
        public void startNettyAfterBoot(ApplicationReadyEvent event) throws InterruptedException {
            nettyServerManager.startServer();
        }
    }
}

