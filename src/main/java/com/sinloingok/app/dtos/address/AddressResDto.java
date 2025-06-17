package com.sinloingok.app.dtos.address;

import lombok.Data;

/**
 * 请求实体，用于新增场所接口。
 */
@Data
public class AddressResDto {
    private String addrName;
    private String addrPersonName;
    private String addrPersonMobile;
    private String img;
    private String imges;
    private String shouYe;
    private String huoDong;
    private String chongZhi;
}
