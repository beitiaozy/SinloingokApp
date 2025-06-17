package com.sinloingok.app.dtos.common;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class NetSiteInfoDto {

    private String name;
    private String status;
    private Integer baseTimeX;
    private Float priceY;
    private Float priceZ;
    private String addrName;
    private String addrPersonName;
    private String addrPersonMobile;
    private Long userId;
    private Long id;
    private Long addressId;
    private String shouYe;
    private String huoDong;
    private String chongZhi;
    private String imges;
    private String type;
    private Float prizePm;
    private Float prizeSl;
    private String rechargeDescription;

    private Float money;   // 平台显示合计金额
    private Float extMoney; // 赠送金额

    private List<Map<String, BigDecimal>> freeMap;

}
