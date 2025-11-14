package com.sinloingok.app.constant;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonParams {
    // 按文档公共请求参数定义
    private Integer appId;          // eg: 1314
    private Integer areaId;         // 固定 0
    private Integer endpointOS;     // 1:Windows 2:Linux 3:iOS 4:Android...
    private String clientVersion;   // 可空
    private String endpointIP;      // 可空
    private String dataTag;         // 可空
    private String context;         // 必传：客户端生成的 32 位 GUID
    private String locale;          // 默认为 zh_CN
    private String deviceId;        // 客户端唯一设备ID（32位MD5）
    private String client_id;       // 商户ID

    private String client_secret;
    // 可按需扩展
    // getter/setter ...
}
