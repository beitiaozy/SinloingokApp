package com.sinloingok.app.models.bluetooth;

import lombok.Data;

/**
 * 场所
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
public class BluetoothAddress {
    private static final long serialVersionUID = -6009680277019724808L;


    private Long id;
    private Long userId;
    private String shortName;
    private String addrName;
    private String addrPersonName;
    private String addrPersonMobile;
    private String img;
    private String imges;
    private String status;
    private String createTime;
    private String updateTime;
    private String wxMchid;
    private String wxapiv2PrivateKey;
    private String shouYe;
    private String huoDong;
    private String chongZhi;
    private Integer newRechargeDay;
    private Float newRechargeMoney;
    private Float newRechargeAwards;

}
