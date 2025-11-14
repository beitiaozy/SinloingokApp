package com.sinloingok.app.util.smyoo;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public final class DatapointBuilder {
    private final Map<String, Object> map = new LinkedHashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static DatapointBuilder create() { return new DatapointBuilder(); }
    public DatapointBuilder put(String k, Object v) { map.put(k, v); return this; }
    public String json() {
        try { return MAPPER.writeValueAsString(map); }
        catch (Exception e) { throw new IllegalStateException("build datapoint json error", e); }
    }
}
