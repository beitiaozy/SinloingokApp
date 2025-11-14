package com.sinloingok.app.util.smyoo;

import okhttp3.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class HttpTransport {
    private final OkHttpClient client;
    private volatile String bpeSessionId; // 登录态 Cookie 值

    public HttpTransport() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    private final Map<HttpUrl, List<Cookie>> store = new HashMap<>();
                    @Override public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        store.put(url, cookies);
                        // 抓取并缓存 BpeSessionId
                        cookies.stream()
                                .filter(c -> "BpeSessionId".equalsIgnoreCase(c.name()))
                                .findFirst()
                                .ifPresent(c -> bpeSessionId = c.value());
                    }
                    @Override public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> list = store.getOrDefault(url, Collections.emptyList());
                        // 若我们已记录 bpeSessionId，但 store 里无，则补上
                        if (bpeSessionId != null) {
                            List<Cookie> merged = new ArrayList<>(list);
                            merged.add(new Cookie.Builder()
                                    .domain(url.host())
                                    .path("/")
                                    .name("BpeSessionId")
                                    .value(bpeSessionId)
                                    .httpOnly()
                                    .build());
                            return merged;
                        }
                        return list;
                    }
                })
                .build();
    }

    public String getBpeSessionId() { return bpeSessionId; }
    public void setBpeSessionId(String sid) { this.bpeSessionId = sid; }

    public Response postJson(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request req = new Request.Builder().url(url).post(body).build();
        return client.newCall(req).execute();
    }
}
