package com.sinloingok.app.simulator;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

final class WashProto {

    /** 把 8 路开关位打成 1 字节位图：bit0=通道1 … bit7=通道8 */
    static int maskOf(int... channelsOn) {
        int m = 0;
        for (int ch : channelsOn) {
            if (ch < 1 || ch > 8) continue;
            m |= (1 << (ch - 1));
        }
        return m & 0xFF;
    }

    /** 生成一帧：EE FF C0 01 01 00 00 01 [open 2B] [close 2B] 00 C2
     *  - 12/13 字节：开位图（低位在前）  14/15 字节：关位图
     *  - 其他字节按你例子占位固定（可按需调整）
     */
    static byte[] buildFrame(int openMask, int closeMask) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(0xEE).writeByte(0xFF).writeByte(0xC0);
        buf.writeByte(0x01);              // 设备/功能
        buf.writeByte(0x01).writeByte(0x00).writeByte(0x00).writeByte(0x01); // 保留
        // 开位图（2 字节，小端：低字节在前）
        buf.writeByte(openMask & 0xFF);
        buf.writeByte(0x00);
        // 关位图（2 字节，小端）
        buf.writeByte(closeMask & 0xFF);
        buf.writeByte(0x00);
        buf.writeByte(0x00);              // 预留
        buf.writeByte(0xC2);              // 尾
        byte[] out = new byte[buf.readableBytes()];
        buf.readBytes(out);
        return out;
    }

    /** 便捷：给出“目标开着的通道集合”，自动得出 open/close */
    static byte[] frameByOnChannels(int... onChannels) {
        int open = maskOf(onChannels);
        int close = (~open) & 0xFF;
        return buildFrame(open, close);
    }

    static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte v : b) sb.append(String.format("%02X", v));
        return sb.toString();
    }
}
