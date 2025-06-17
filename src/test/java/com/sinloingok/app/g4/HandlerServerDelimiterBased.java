package com.sinloingok.app.g4;

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
 * @Data 2021/4/13 15:15
 * @Version 1.0
 * @Description 服务器端I/O处理类,连接监测,分隔符解码器
 */
@ChannelHandler.Sharable
public class HandlerServerDelimiterBased extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //处理收到的数据,并反馈消息给到客户端
        String in = (String) msg;
        System.out.println("收到的客户端消息 : " + in.trim());

        //写入并发送消息给到远程(客户端)
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss SSS");//设置日期格式
        String strDate = df.format(new Date());
        String strMsg = "你好,客户端" + strDate + "$";
        System.out.println("向客户端反馈的消息 : " + strMsg);
        ctx.writeAndFlush(Unpooled.copiedBuffer(strMsg, CharsetUtil.UTF_8));
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

