package com.sinloingok.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

// com.sinloingok.app.util.http.HttpUtil
public class HttpUtil {
    public static String postJson(String urlStr, String jsonBody, String authorization) {
        StringBuilder response = new StringBuilder();
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            if (authorization != null) {
                conn.setRequestProperty("Authorization", authorization);
            }
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) response.append(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("HTTP POST 失败: " + e.getMessage(), e);
        } finally {
            if (conn != null) conn.disconnect();
        }
        return response.toString();
    }
}
