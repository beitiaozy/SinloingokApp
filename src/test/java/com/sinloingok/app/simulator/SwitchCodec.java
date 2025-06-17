package com.sinloingok.app.simulator;

import java.util.*;
import java.util.stream.IntStream;

public class SwitchCodec {

    // 解析：返回 {action, channel, begin, end}
    public static Map<String, Object> parse(String hex) {
        if (hex == null || hex.length() < 16) throw new IllegalArgumentException("hex too short");
        String begin = hex.substring(12, 14);
        String end   = hex.substring(14, 16);

        String action = "00".equals(begin) ? "CLOSE" : "OPEN";
        int ch = convert(!"00".equals(begin) ? begin : end);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("action", action);
        m.put("channel", ch);
        m.put("begin", begin);
        m.put("end", end);
        return m;
    }

    // 按你服务端取位规则构造：12-13=begin低/高(固定高=00), 14-15=end低/高(固定高=00)
    public static String buildOpen(int channel) {
        String beginLow = to2Hex(1 << (channel - 1));
        return "eeffc001010000" + beginLow + "00" + "c2";
        //            0..11    [12..13] [14..15]
    }

    public static String buildClose(int channel) {
        String endLow = to2Hex(1 << (channel - 1));
        return "eeffc001010000" + "00" + endLow + "c2";
    }

    // convert：把 "01/02/04/08/10/20/40/80" 映射为 1..8
    public static int convert(String twoHex) {
        double v = Double.parseDouble(Integer.toString(Integer.parseInt(twoHex, 16))); // 与你服务端等价
        int r = (int)(Math.log(v) / Math.log(2)) + 1;
        return Math.max(r, 0);
    }

    private static String to2Hex(int v) {
        return String.format("%02x", v & 0xFF);
    }

    // 预生成一个 Map：channel -> {openHex, closeHex}
    public static Map<Integer, Map<String, String>> prebuiltFrames() {
        Map<Integer, Map<String, String>> map = new LinkedHashMap<>();
        IntStream.rangeClosed(1, 8).forEach(ch -> {
            Map<String, String> pair = new LinkedHashMap<>();
            pair.put("open",  buildOpen(ch));
            pair.put("close", buildClose(ch));
            map.put(ch, pair);
        });
        return map;
    }

    // demo
    public static void main(String[] args) {
        String hex = "eeffc00101000001c2";
        System.out.println(parse(hex)); // {action=CLOSE, channel=1, begin=00, end=01}

        System.out.println(SwitchProtoCompat.buildOpen(7));  // eef... 12-13=40,14-15=00
        System.out.println(SwitchProtoCompat.buildClose(7)); // eef... 12-13=00,14-15=40

        Map<Integer, Map<String,String>> frames = prebuiltFrames();
        System.out.println(frames.get(1).get("open"));  // 开1号
        System.out.println(frames.get(1).get("close")); // 关1号
        System.out.println("======================");
        for(int i = 1; i < 9; i ++){
            String beginSignal =SwitchProtoCompat.buildOpen(i);
            String endSignal = SwitchProtoCompat.buildClose(i);
            System.out.println("beginSignal : " + beginSignal);
            System.out.println(parse(beginSignal));
            System.out.println();
            System.out.println("endSignal : " + endSignal);
            System.out.println(parse(endSignal));
            System.out.println("======================");
        }
    }
}
