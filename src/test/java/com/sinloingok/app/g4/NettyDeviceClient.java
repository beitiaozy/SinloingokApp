package com.sinloingok.app.g4;

import com.google.common.collect.Lists;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 模擬終端設備連接
 */
@Slf4j
public class NettyDeviceClient {

    private final String host;
    private final int port;
    private final String onlyCode;

    public NettyDeviceClient(String host, int port, String onlyCode) {
        this.host = host;
        this.port = port;
        this.onlyCode = onlyCode;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.SO_KEEPALIVE, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ch.pipeline().addLast(new SimpleChannelInboundHandler<Object>() {
                         @Override
                         public void channelActive(ChannelHandlerContext ctx) {
                             log.info("设备 {} 已连接到服务器", onlyCode);
                             // 模拟心跳报文，随便拼一个
                             String heartbeatHex = "a50012594a" + str2Hex(onlyCode);
                             ctx.writeAndFlush(Unpooled.wrappedBuffer(hexString2Bytes(heartbeatHex)));
                         }

                         @Override
                         protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                             log.info("设备 {} 收到数据: {}", onlyCode, msg);
                         }
                     });
                 }
             });

            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    private static byte[] hexString2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = (byte) Integer.parseInt(src.substring(i * 2, i * 2 + 2), 16);
        }
        return ret;
    }

    private static String str2Hex(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            sb.append(Integer.toHexString((int) c));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        List<String> list = Lists.newArrayList("0090D500245B", "0090D5000F10", "0090D500242A", "0090D5000F4A"); // 模擬5台設備
        for (String onlyCode : list) {
            new Thread(() -> {
                NettyDeviceClient client = new NettyDeviceClient("127.0.0.1", 18080, onlyCode);
                try {
                    client.start();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(client.onlyCode);
            }).start();
        }
    }
}
