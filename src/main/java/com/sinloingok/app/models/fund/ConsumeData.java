package com.sinloingok.app.models.fund;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ConsumeData {
    private UserAddressAccount account;
    private FundTransaction transaction;


    public BigDecimal getAmount(){
        return account.getBalance().add(account.getExtMoney());
    }
}
