package com.sinloingok.app.g4;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author 吴世俊
 * @Data 2021/4/12 14:55
 * @Version 1.0
 * @Description 客户端启动类
 */
public class AppClientEnDecoder {

    private final String host;
    private final int port;

    public AppClientEnDecoder(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception{
        /**
         * 配置相应的参数,提供连接到远端的方法
         */
        EventLoopGroup group = new NioEventLoopGroup(); //I/O线程池

        try {
            Bootstrap bs = new Bootstrap();//客户端辅助启动类

            bs.group(group)
                    .channel(NioSocketChannel.class)//实例化一个Channel
                    .remoteAddress(new InetSocketAddress(host,port))//制定远端的IP和端口
                    .handler(new InitializerClientEnDecoder());

            //连接到远程节点;等待连接完成
            ChannelFuture future = bs.connect().sync();

            //阻塞操作,closeFuture()开启了一个channel监听器(这期间channel在进行各项工作),直到链路断开
            future.channel().closeFuture().sync();
        } finally {
            //优雅关闭(彻彻底底的释放资源)
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception{
//        new AppClientEnDecoder("47.94.103.164",18080).run();
        new AppClientEnDecoder("127.0.0.1",18080).run();
    }
}
