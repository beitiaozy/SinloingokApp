package com.sinloingok.app.util.ns;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 公共的信號報文工具方法。
 */
public final class SignalPayloadUtils {

    private static final Map<String, String> BIT_REVERSE_TABLE;

    static {
        Map<String, String> bitReverseTable = new LinkedHashMap<>();
        bitReverseTable.put("00", "00");
        bitReverseTable.put("01", "80");
        bitReverseTable.put("02", "40");
        bitReverseTable.put("04", "20");
        bitReverseTable.put("08", "10");
        bitReverseTable.put("10", "08");
        bitReverseTable.put("20", "04");
        bitReverseTable.put("40", "02");
        bitReverseTable.put("80", "01");
        BIT_REVERSE_TABLE = Collections.unmodifiableMap(bitReverseTable);
    }

    private SignalPayloadUtils() {
    }

    public static int convert(String input) {
        try {
            int intValue = Integer.parseInt(input, 16);
            int result = (int) (Math.log(intValue) / Math.log(2)) + 1;
            return result < 0 ? 0 : result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("输入不是有效数字: " + input);
        }
    }

    public static String reverseBitsLookup(String hexByte) {
        if (hexByte == null || hexByte.length() != 2) return "00";
        return BIT_REVERSE_TABLE.getOrDefault(hexByte.toUpperCase(), "00");
    }

    public static String safeSub(String s, int start, int end) {
        if (s == null) return "";
        if (start < 0) start = 0;
        if (end > s.length()) end = s.length();
        if (start >= end) return "";
        return s.substring(start, end);
    }

    public static byte[] hexStringToBytes(String hexString) {
        int length = hexString.length();
        if ((length & 1) == 1) {
            throw new IllegalArgumentException("hex 长度必须为偶数: " + length);
        }
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            int hi = Character.digit(hexString.charAt(i), 16);
            int lo = Character.digit(hexString.charAt(i + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("非法hex字符: " + hexString.substring(i, i + 2));
            }
            data[i / 2] = (byte) ((hi << 4) + lo);
        }
        return data;
    }
}
