package com.sinloingok.app.models.net4g;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class NetSiteBillRule {
    private Long id;
    private String ruleCode;
    private String ruleName;
    private BigDecimal unitPrice;
    private Integer minBillDuration;
    private Integer maxBillDuration;
    private BigDecimal maxPrice;
    private Long addressId;
    private Integer channelNo;
    private Date createTime;
    private Date updateTime;
    private Boolean isActive;

    @Override
    public String toString() {
        return "BillingRule{" +
                "id=" + id +
                ", ruleCode='" + ruleCode + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", unitPrice=" + unitPrice +
                ", minBillDuration=" + minBillDuration +
                ", maxBillDuration=" + maxBillDuration +
                ", maxPrice=" + maxPrice +
                ", addressId=" + addressId +
                ", channelNo=" + channelNo +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", isActive=" + isActive +
                '}';
    }
}