package com.sinloingok.app.models.net4g;

import lombok.Data;

/**
 * 设备接收到的报文信息
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
public class NetSiteMessage {
    private static final long serialVersionUID = 5968825670095069087L;

    private Long id;
    private String onlyCode;
    private String msg;
    private Integer type;
    private String createTime;

}

