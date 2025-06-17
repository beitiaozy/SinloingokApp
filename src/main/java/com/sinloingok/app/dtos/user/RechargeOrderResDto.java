package com.sinloingok.app.dtos.user;

import com.sinloingok.app.dao.status.GenericRecharge;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
public class RechargeOrderResDto implements Serializable {
    private Long rechargeId;
    private String code;
    private String createTime;
    private String status;
    private Long userId;
    private String nickname;
    private String mobile;
    private Float money;   // 平台显示合计金额
    private Float extMoney; // 赠送金额
    private float orderMoney;
    private Float orderExtMoney;
    private Long addressId;
    private String shortName;
    private String addrName;
    private String addrPersonName;
    private String addrPersonMobile;
    private String type;//充值类型

    public void setType(String type) {
        if(StringUtils.isNotEmpty(type)){
            this.type = GenericRecharge.fromCode(type.toUpperCase()).getGenreName();
        }
    }
}
