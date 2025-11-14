package com.sinloingok.app.util.smyoo;

import com.sinloingok.app.constant.CommonParams;
import java.util.UUID;

/**
 * 工具類：提供便捷的公共參數構建方法，保持與測試示例一致。
 */
public final class ParamsFactory {
    private ParamsFactory(){}

    public static CommonParams of(int appId, String deviceId, String clientId, int endpointOS) {
        CommonParams p = new CommonParams();
        p.setAppId(appId);
        p.setAreaId(0);
        p.setEndpointOS(endpointOS);
        p.setClientVersion("");
        p.setContext("A3C64845E49049BF8EA4027B6828CEF1");
        p.setLocale("zh_CN");
        p.setDeviceId(deviceId);
        p.setClient_id(clientId);
        return p;
    }
}
