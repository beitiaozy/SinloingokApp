package com.sinloingok.app.util.smyoo;

import com.sinloingok.app.constant.CommonParams;
import java.util.UUID;

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
