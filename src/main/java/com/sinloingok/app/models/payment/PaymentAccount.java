package com.sinloingok.app.models.payment;

import lombok.Data;

import java.util.Date;

@Data
public class PaymentAccount {
    private Long id;
    private String channel;
    private String name;
    private String vendorSn;
    private String vendorKey;
    private String appId;
    private String apiUrl;
    private String notifyUrlRecharge;
    private String wechatSubAppId;
    private String platformPublicKey;
    private Integer isEnabled;
    private Date gmtCreate;
    private Date gmtModified;
}
