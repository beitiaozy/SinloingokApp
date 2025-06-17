
package com.sinloingok.app.util.pay;

import java.security.MessageDigest;
/**
 * Utility for generating MD5 based signatures used by the payment API.
 */
public class SignUtil {

    /**
     * Create an uppercase MD5 signature using the body and secret key.
     *
     * @param body request body
     * @param key secret key
     * @return generated signature
     */
    public static String sign(String body, String key) {
        String data = body + key;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("签名生成失败", e);
        }
    }
}
