package com.sinloingok.app.simulator;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

class DeviceHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final DeviceClient owner;
    public DeviceHandler(DeviceClient owner) { this.owner = owner; }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        owner.onActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        owner.onInactive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        byte[] arr = new byte[msg.readableBytes()];
        msg.readBytes(arr);
        owner.onMessage(arr);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("pipeline异常: " + cause);
        ctx.close();
    }
}
