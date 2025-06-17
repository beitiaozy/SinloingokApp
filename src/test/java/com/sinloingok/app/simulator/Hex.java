package com.sinloingok.app.simulator;

class Hex {
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte v : b) sb.append(HEX[(v >> 4) & 0xF]).append(HEX[v & 0xF]);
        return sb.toString();
    }

    static byte[] fromHex(String s) {
        String str = s.replaceAll("\\s+","");
        int len = str.length();
        if ((len & 1) == 1) throw new IllegalArgumentException("hex length must be even");
        byte[] out = new byte[len/2];
        for (int i=0;i<out.length;i++) {
            int hi = Character.digit(str.charAt(i*2),16);
            int lo = Character.digit(str.charAt(i*2+1),16);
            out[i] = (byte)((hi<<4)|lo);
        }
        return out;
    }
}
