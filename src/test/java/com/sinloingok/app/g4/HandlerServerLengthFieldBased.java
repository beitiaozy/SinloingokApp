package com.sinloingok.app.g4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author 吴世俊
 * @Data 2021/4/13 16:45
 * @Version 1.0
 * @Description 服务器端,自定义长度解码器
 */
@ChannelHandler.Sharable
public class HandlerServerLengthFieldBased extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //读取消息,并按照自定义的协议格式进行消息的处理
        ByteBuf in = (ByteBuf) msg;

        //打印报文
        String srcInfo = ByteBufUtil.hexDump(in).toUpperCase();//把字节序列转化为16进制,并且转化为大写
        System.out.println("收到的原始报文 : " + srcInfo);

        //报文解析
        short header = in.readShort();
        int msgType = in.readByte();
        int contentLen = in.readInt();
        ByteBuf bufContent = in.readBytes(contentLen);
        String content = bufContent.toString(CharsetUtil.UTF_8);

        System.out.println("收到的消息为 : " + content);

        //给客户端反馈信息
        short header2 = 0x3C3C;
        byte[] msgtype2 = new byte[]{(byte) 0x01};
        String strContent2 = "你好,宝塔镇河妖!";
        int contentLen2 = strContent2.getBytes().length;

        //写入通道
        ctx.write(Unpooled.copyShort(header2));
        ctx.write(Unpooled.copiedBuffer(msgtype2));
        ctx.write(Unpooled.copyInt(contentLen2));
        ctx.write(Unpooled.copiedBuffer(strContent2,CharsetUtil.UTF_8));

        //发送消息
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //出现异常时执行的动作(打印并且关闭通道)
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //新建立连接时触发的动作
        Channel incoming = ctx.channel();
        System.out.println("客户端: " + incoming.remoteAddress() + "已连接上来");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //连接断开时触发的动作
        Channel incoming = ctx.channel();
        System.out.println("客户端: " + incoming.remoteAddress() + "已断开");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //通道处于非活动状态触发的动作,该方法只会在通道失效时调用一次
        Channel incoming = ctx.channel();
        System.out.println("客户端: " + incoming.remoteAddress() + "掉线");
    }
}
