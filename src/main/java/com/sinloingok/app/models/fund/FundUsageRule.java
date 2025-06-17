package com.sinloingok.app.models.fund;

import lombok.Data;
import java.util.Date;

@Data
public class FundUsageRule {
    private Long id;
    private Long sourceAddressId;
    private Long targetAddressId;
    /**
     * 1表示可以使用
     */
    private Integer status;
    private Date createTime;
}