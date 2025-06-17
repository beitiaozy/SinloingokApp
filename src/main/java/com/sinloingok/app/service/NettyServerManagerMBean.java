package com.sinloingok.app.service;

/**
 * Netty服务器管理器的JMX MBean接口
 * 用于监控服务器状态，可以通过JConsole、VisualVM等工具查看
 */
public interface NettyServerManagerMBean {

    /**
     * 获取服务器当前运行状态
     */
    boolean isServerRunning();

    /**
     * 获取当前活跃连接数
     */
    int getConnectionCount();

    /**
     * 获取Boss线程组线程数
     */
    int getBossThreads();

    /**
     * 获取Worker线程组线程数
     */
    int getWorkerThreads();

    /**
     * 手动重启服务器（通过JMX调用）
     */
    void restartServer();
}