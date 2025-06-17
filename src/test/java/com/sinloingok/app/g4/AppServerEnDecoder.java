package com.sinloingok.app.g4;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Author 吴世俊
 * @Data 2021/4/12 15:36
 * @Version 1.0
 * @Description 服务器端启动类
 */
public class AppServerEnDecoder {

    private int port;

    public AppServerEnDecoder(int port) {
        this.port = port;
    }

    public void run() throws Exception{

        //负责接收客户端的连接
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //负责处理消息I/O
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();//用于启动NIO服务

            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)//通过工厂方法设计模式实例化一个Channel
                    .localAddress(new InetSocketAddress(port))//设置监听端口
                    .option(ChannelOption.SO_BACKLOG,128)//最大保持连接数128,option主要是针对boss线程组
                    .childOption(ChannelOption.SO_KEEPALIVE,true)//启用心跳保活机制,childOption主要是针对work线程组
                    .childHandler(new InitializerServerEnDecoder());

            //绑定服务器,该实例提供有关IO操作的结果或状态的信息
            ChannelFuture channelFuture = b.bind().sync();
            System.out.println("在" + channelFuture.channel().localAddress() + "上开启监听");

            //阻塞操作,closeFuture()开启了一个Channel的监听器(这期间Channel在进行各项工作),直到链路断开
            channelFuture.channel().closeFuture().sync();
        } finally {
            //关闭EventLoopGroup并释放所有资源,包括所有创建的线程
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        }
    }
}