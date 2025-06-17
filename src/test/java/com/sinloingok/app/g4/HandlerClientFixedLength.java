package com.sinloingok.app.g4;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * @Author 吴世俊
 * @Data 2021/4/12 14:31
 * @Version 1.0
 * @Description 客户端,定长解码器
 */

@ChannelHandler.Sharable
public class HandlerClientFixedLength extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String info) throws Exception {
        /**
         * 处理接收到的消息
         */
        System.out.println("接收到的消息: " + info.trim());
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
        //建立连接后该channelActive()方法只会调用一次,这里的逻辑:建立连接后,字节序列被发送到服务器,编码格式是utf-8
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss SSS");//设置日期格式
        String strDate = df.format(new Date());
        String strMsg = "你好,服务器" + strDate;

        //由于是发送定长消息,我们需要判断消息的长度,如果小于指定长度则补充空格
        strMsg = CommonTools.formatString(strMsg,CommonTools.FIXEDLENGTHFRAME_LENGTH);
        System.out.println("已经连上服务器,现在发送一条消息 : " + strMsg);
        ctx.writeAndFlush(Unpooled.copiedBuffer(strMsg,CharsetUtil.UTF_8));
    }
}
