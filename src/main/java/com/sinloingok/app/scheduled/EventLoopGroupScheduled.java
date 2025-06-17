package com.sinloingok.app.scheduled;

import com.sinloingok.app.service.NettyServerManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EventLoopGroupScheduled {

    private NettyServerManager nettyServerManager;

    // 通过构造器注入
    public EventLoopGroupScheduled(NettyServerManager nettyServerManager) {
        this.nettyServerManager = nettyServerManager;
    }

    @Scheduled(cron = "0 1 0 * * ?") // 每天00:01:00执行
    public void dailyRestart() {
        // 只需调用重启方法，该方法会立即返回，不会阻塞定时任务线程
        nettyServerManager.restartServer();
    }
}
