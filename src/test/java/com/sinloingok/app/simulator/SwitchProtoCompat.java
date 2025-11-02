package com.sinloingok.app.simulator;

/**
 * 为兼容你服务端当前实现：
 *  - hex.substring(12,14) = begin 低字节
 *  - hex.substring(14,16) = end   低字节
 * 构造： "eeffc0010100" + begin(2) + end(2) + "c2"
 */
public class SwitchProtoCompat {

    // 开：begin=2^(n-1), end=00
    public static String buildOpen(int channel) {
        channel = 9 - channel;
        return buildRaw(ch2(channel), "00");
    }

    // 关：begin=00, end=2^(n-1)
    public static String buildClose(int channel) {
        channel = 9 - channel;
        return buildRaw("00", ch2(channel));
    }

    private static String buildRaw(String begin2Hex, String end2Hex) {
        return ("eeffc0010100" + begin2Hex + end2Hex + "c2").toUpperCase();
    }

    private static String ch2(int ch) {
        if (ch<1 || ch>8) throw new IllegalArgumentException("channel 1..8");
        int v = 1 << (ch - 1);
        return String.format("%02X", v & 0xFF);
    }
}
