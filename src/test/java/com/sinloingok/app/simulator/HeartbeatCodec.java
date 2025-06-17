package com.sinloingok.app.simulator;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

class HeartbeatCodec {
    private static final byte[] PREFIX = Hex.fromHex("A50012594A");

    /** a50012594a + onlyCode(ASCII 12B) */
    static byte[] encode(String onlyCode) {
        byte[] ascii12 = onlyCode.getBytes(StandardCharsets.US_ASCII);
        ByteBuf buf = Unpooled.buffer(PREFIX.length + ascii12.length);
        buf.writeBytes(PREFIX);
        buf.writeBytes(ascii12);
        byte[] out = new byte[buf.readableBytes()];
        buf.readBytes(out);
        return out;
    }
}
