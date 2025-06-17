package com.sinloingok.app.util;

import com.sinloingok.app.config.SBeanUtils;
import com.sinloingok.app.dao.FundTransactionDao;
import com.sinloingok.app.models.fund.FundTransaction;

import java.math.BigDecimal;
import java.util.Date;

public class FundTransactionUtil {
    public static FundTransaction build(Long userId, Long addressId, String payCode,
                                        BigDecimal balanceBefore, BigDecimal balanceAfter,
                                        BigDecimal extBefore, BigDecimal extAfter,
                                        BigDecimal amount, Integer type, String remark, String message) {
        FundTransaction tx = new FundTransaction();
        tx.setUserId(userId);
        tx.setAddressId(addressId);
        tx.setSourceAddressId(addressId);
        tx.setPayCode(payCode);
        tx.setBalanceBefore(balanceBefore);
        tx.setBalanceAfter(balanceAfter);
        tx.setExtMoneyBefore(extBefore);
        tx.setExtMoneyAfter(extAfter);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setStatus(1);
        tx.setRemark(remark);
        tx.setMessage(message);
        tx.setCreateTime(new Date());
        return tx;
    }


    public static void create(Long userId, Long addressId, String payCode,
                                        BigDecimal balanceBefore, BigDecimal balanceAfter,
                                        BigDecimal extBefore, BigDecimal extAfter,
                                        BigDecimal amount, Integer type, String remark, String message) {
        FundTransaction tx = build(userId, addressId, payCode, balanceBefore, balanceAfter, extBefore, extAfter, amount, type, remark, message);
        SBeanUtils.getBean(FundTransactionDao.class).insert(tx);
    }
}
