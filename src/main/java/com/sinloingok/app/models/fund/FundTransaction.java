package com.sinloingok.app.models.fund;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class FundTransaction {
    private Long id;
    private Long userId;
    private Long addressId;
    private Long sourceAddressId;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private BigDecimal extMoneyBefore;
    private BigDecimal extMoneyAfter;
    private String payCode;
    // 1 充值，2 消費，3 後台修改，4，月卡充值，5次卡充值，6，優惠券購買
    private Integer type;
    private Integer status;
    private String remark;
    private String message;
    private Date createTime;
}