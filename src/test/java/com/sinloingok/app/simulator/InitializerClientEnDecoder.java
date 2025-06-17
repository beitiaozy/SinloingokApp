package com.sinloingok.app.simulator;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;

public class InitializerClientEnDecoder extends ChannelInitializer<io.netty.channel.socket.SocketChannel> {
    @Override
    protected void initChannel(io.netty.channel.socket.SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        // 直接用原始字节发送，不额外编码器也行：
        p.addLast("client", new SimpleChannelInboundHandler<io.netty.buffer.ByteBuf>() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                // 连接成功就启动洗车流程
                runWashFlow(ctx);
            }

            @Override
            protected void channelRead0(ChannelHandlerContext ctx, io.netty.buffer.ByteBuf msg) {
                // 如需打印回包
                byte[] arr = new byte[msg.readableBytes()];
                msg.readBytes(arr);
                System.out.println("RESP HEX: " + WashProto.toHex(arr));
            }

            private void sendFrame(ChannelHandlerContext ctx, byte[] frame, String tag) {
                System.out.println(tag + " -> " + WashProto.toHex(frame));
                ctx.writeAndFlush(Unpooled.wrappedBuffer(frame));
            }

            /** 洗车时序（可微调时间，单位秒） */
            private void runWashFlow(ChannelHandlerContext ctx) {
                final EventLoop loop = ctx.channel().eventLoop();

                // T0 预冲（开进水阀=1，水泵=2）
                loop.schedule(() ->
                        sendFrame(ctx, WashProto.frameByOnChannels(1, 2), "T0 预冲 开(1,2)"),
                        0, java.util.concurrent.TimeUnit.SECONDS);

                // T+20 关预冲
                loop.schedule(() ->
                        sendFrame(ctx, WashProto.frameByOnChannels(), "T+20 预冲 关"),
                        20, java.util.concurrent.TimeUnit.SECONDS);

                // T+21 喷泡沫（泡沫阀=3，水泵=2）
                loop.schedule(() ->
                        sendFrame(ctx, WashProto.frameByOnChannels(2, 3), "T+21 泡沫 开(2,3)"),
                        21, java.util.concurrent.TimeUnit.SECONDS);

                // T+60 进入刷洗（保留水泵=2，开刷子=4，提示灯=7）
                loop.schedule(() ->
                        sendFrame(ctx, WashProto.frameByOnChannels(2, 4, 7), "T+60 刷洗 开(2,4,7)"),
                        60, java.util.concurrent.TimeUnit.SECONDS);

                // T+120 关闭刷子，仅清水冲（清水阀=5，水泵=2）
                loop.schedule(() ->
                        sendFrame(ctx, WashProto.frameByOnChannels(2, 5), "T+120 清水冲 开(2,5)"),
                        120, java.util.concurrent.TimeUnit.SECONDS);

                // T+160 风干（开风机=6，提示灯=7）
                loop.schedule(() ->
                        sendFrame(ctx, WashProto.frameByOnChannels(6, 7), "T+160 风干 开(6,7)"),
                        160, java.util.concurrent.TimeUnit.SECONDS);

                // T+190 全关（安全复位）
                loop.schedule(() ->
                        sendFrame(ctx, WashProto.frameByOnChannels(), "T+190 全关"),
                        190, java.util.concurrent.TimeUnit.SECONDS);
            }
        });
    }
}
