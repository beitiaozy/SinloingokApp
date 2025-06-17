package com.sinloingok.app.dtos.user;

import com.sinloingok.app.dtos.DateRangeQuery;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class RechargeRecordDto extends DateRangeQuery {

    private String createTime;
    private BigDecimal amount;
    private BigDecimal money;
    private BigDecimal awards;
    private BigDecimal balanceBefore;
    private BigDecimal extMoneyBefore;
    private int type;
    private String remark;
    private String payCode;


    private String addrName;
    private String mobile;
    private long addressId;
    private long userId;
    private String status;

    private Map<String, String> attr = new HashMap<>();

    public void addAttr(String key, String value){
        attr.put(key, value);
    }
}
