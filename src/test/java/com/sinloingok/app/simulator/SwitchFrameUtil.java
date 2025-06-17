package com.sinloingok.app.simulator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * 开关量主动上传协议（文档）：
 * EE FF C0 01 SL KL OH OL
 * - SL: 继电器状态（占位）
 * - KL: 当前所有输入状态(按需，这里默认与通道一致)
 * - OH: 本次触发(上升沿)
 * - OL: 本次消失(下降沿)
 */
public class SwitchFrameUtil {

    /** 生成指定通道的上升沿/下降沿上传帧（文档版） */
    public static String buildUploadFrame(int channel, boolean risingEdge) {
        if (channel<1 || channel>8) throw new IllegalArgumentException("channel 1..8");
        String prefix = "EEFFC001";
        String SL = "00";
        String KL = to2Hex(1 << (channel - 1));
        String OH = risingEdge ? KL : "00";
        String OL = risingEdge ? "00" : KL;
        return (prefix + SL + KL + OH + OL).toUpperCase();
    }

    /** 预生成 1..8 的上升/下降帧 */
    public static Map<Integer, Map<String, String>> prebuiltFrames() {
        Map<Integer, Map<String,String>> m = new LinkedHashMap<>();
        IntStream.rangeClosed(1,8).forEach(ch -> {
            Map<String,String> p = new LinkedHashMap<>();
            p.put("rise", buildUploadFrame(ch,true));
            p.put("fall", buildUploadFrame(ch,false));
            m.put(ch,p);
        });
        return m;
    }

    private static String to2Hex(int v) {
        return String.format("%02X", v & 0xFF);
    }
}
