package com.sinloingok.app.util.smyoo;

import com.sinloingok.app.constant.CommonParams;

/**
 * 工具類：提供便捷的公共參數構建方法，保持與測試示例一致。
 */
public final class ParamsFactory {

    private ParamsFactory() {
    }

    public static CommonParams of(Integer uid, String deviceId, String clientId, Integer protocol) {
        CommonParams params = new CommonParams();
        params.setUid(uid);
        params.setDeviceid(deviceId);
        params.setClientId(clientId);
        params.setProtocol(protocol);
        return params;
    }
}

