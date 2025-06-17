package com.sinloingok.app.g4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;


/**
 * @Author 吴世俊
 * @Data 2021/4/13 16:18
 * @Version 1.0
 * @Description 客户端,自定义长度解码器
 */
@ChannelHandler.Sharable
public class HandlerClientLengthFieldBased extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object info) throws Exception {
        ByteBuf in = (ByteBuf) info;

        //打印报文
        String srcInfo = ByteBufUtil.hexDump(in).toUpperCase();//把字节序列转化为16进制,并且转化为大写
        System.out.println("收到的原始报文 : " + srcInfo);

        //报文解析
        short header = in.readShort();
        int msgtype = in.readByte();
        int contentLen = in.readInt();
        ByteBuf bufContent = in.readBytes(contentLen);
        String content = bufContent.toString(CharsetUtil.UTF_8);

        System.out.println("收到的消息为 : " + content);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        /**
         * 处理I/O事件的异常
         */
        //控制台输出
        cause.printStackTrace();

        //关闭连接
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //发送自定义协议格式的消息
        short header = 0x5A5A;
        byte[] msgtype = new byte[]{(byte) 0x01};
        String strContent = "你好,天王盖地虎!";
        int contentLen = strContent.getBytes().length;

        //写入通道
        ctx.write(Unpooled.copyShort(header));
        ctx.write(Unpooled.copiedBuffer(msgtype));
        ctx.write(Unpooled.copyInt(contentLen));
        ctx.write(Unpooled.copiedBuffer(strContent,CharsetUtil.UTF_8));

        //发送消息
        ctx.flush();
    }
}
