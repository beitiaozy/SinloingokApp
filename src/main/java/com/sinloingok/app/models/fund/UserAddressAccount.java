package com.sinloingok.app.models.fund;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class UserAddressAccount {
    private Long id;
    private Long userId;
    private Long addressId;
    private BigDecimal balance;
    private BigDecimal extMoney;
    private Date createTime;
    private Date updateTime;

    public UserAddressAccount(long id, BigDecimal balance, BigDecimal extMoney){
        this.id = id;
        this.balance = balance;
        this.extMoney = extMoney;
    }
    public UserAddressAccount(long userID, long addressId, BigDecimal balance, BigDecimal extMoney){
        this.userId = userID;
        this.addressId = addressId;
        this.balance = balance;
        this.extMoney = extMoney;
    }

}