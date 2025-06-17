package com.sinloingok.app.g4;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
   *@Author 吴世俊
   *@Data 2021/4/13 14:02
   *@Version 1.0
   *@Description 服务器端Handler初始化配置类,在Channel注册到EventLoop后,对这个Channel添加一些初始化的Handler
 */
public class InitializerServerEnDecoder extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //定长解码器FixedLengthFrameDecoder
        //pipeline.addLast("framer",new FixedLengthFrameDecoder(CommonTools.FIXEDLENGTHFRAME_LENGTH));

        //行解码器LineBasedFrameDecoder
        //pipeline.addLast("framer",new LineBasedFrameDecoder(CommonTools.LINEBASEDFRAME_LENGTH,true,true));

        //分隔符解码器DelimiterBasedFrameDecoder
        //ByteBuf delimiter = Unpooled.copiedBuffer("$".getBytes());
        //pipeline.addLast("framer",new DelimiterBasedFrameDecoder(CommonTools.DELIMITERBASEDFRAME_LENGTH,true,true,delimiter));

        //自定义长度解码器LengthFieldBasedFrameDecoder
        pipeline.addLast("framer",new LengthFieldBasedFrameDecoder(1024,3,4,0,0));

        //pipeline.addLast("decoder",new StringDecoder());
        //pipeline.addLast("encoder",new StringEncoder());

        pipeline.addLast(new HandlerServerLengthFieldBased());//自定义业务逻辑Handler
    }
}
