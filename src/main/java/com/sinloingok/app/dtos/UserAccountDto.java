package com.sinloingok.app.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserAccountDto {

    private Long userId;
    private String mobile;
    private Long addressId;
    private BigDecimal balance;
    private BigDecimal extMoney;
    private String remark;
}
