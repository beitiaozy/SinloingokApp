package com.sinloingok.app.constant;

import lombok.Data;

/**
 * 公共請求參數封裝，對應思麓雲喇叭接口所需的公共字段。
 */
@Data
public class CommonParams {

    /** 業務租戶或站點編號。 */
    private Integer uid;

    /** 設備唯一標識。 */
    private String deviceid;

    /** 思麓開放平臺的客戶端 ID。 */
    private String clientId;

    /** 協議版本。 */
    private Integer protocol;

    /** 登錄會話 ID。 */
    private String sessionId;

    /** 驗票後下發的票據。 */
    private String ticket;
}

