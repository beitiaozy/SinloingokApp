package com.sinloingok.app.util.smyoo;

import com.alibaba.fastjson.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 思麓雲喇叭數據點構建器，保留構建順序並輸出 JSON 字串。
 */
public final class DatapointBuilder {

    private final Map<String, Object> values = new LinkedHashMap<>();

    private DatapointBuilder() {
    }

    public static DatapointBuilder create() {
        return new DatapointBuilder();
    }

    public DatapointBuilder put(String key, Object value) {
        values.put(key, value);
        return this;
    }

    public String json() {
        return JSONObject.toJSONString(values);
    }
}

