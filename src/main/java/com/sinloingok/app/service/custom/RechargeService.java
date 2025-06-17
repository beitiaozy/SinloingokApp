//package com.sinloingok.app.service.custom;
//
//import com.sinloingok.app.dao.FundTransactionDao;
//import com.sinloingok.app.dao.UserAddressAccountDao;
//import com.sinloingok.app.models.fund.FundTransaction;
//import com.sinloingok.app.models.fund.UserAddressAccount;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//
//@Service
//public class RechargeService {
//    @Autowired
//    private UserAddressAccountDao accountDao;
//    @Autowired
//    private FundTransactionDao transactionDao;
//
//    @Transactional
//    public void recharge(UserAddressAccount orig, String payCode) {
//        // 1. 获取或创建账户
//        UserAddressAccount account = accountDao.findByUserAndAddress(orig.getUserId(), orig.getAddressId());
//        if (account == null) {
//            account = new UserAddressAccount();
//            account.setUserId(orig.getUserId());
//            account.setAddressId(orig.getAddressId());
//            account.setBalance(BigDecimal.ZERO);
//            account.setExtMoney(BigDecimal.ZERO);
//            accountDao.insert(account);
//        }
//
//        // 2. 计算新余额
//        BigDecimal newBalance = account.getBalance().add(orig.getBalance());
//        BigDecimal newExtMoney = account.getExtMoney().add(orig.getExtMoney());
//
//        // 3. 记录交易流水
//        FundTransaction transaction = new FundTransaction();
//        transaction.setUserId(account.getUserId());
//        transaction.setAddressId(account.getAddressId());
//        transaction.setSourceAddressId(account.getAddressId());
//        transaction.setAmount(orig.getBalance().add(orig.getExtMoney()));
//        transaction.setBalanceBefore(account.getBalance());
//        transaction.setBalanceAfter(newBalance);
//        transaction.setExtMoneyBefore(account.getExtMoney());
//        transaction.setExtMoneyAfter(newExtMoney);
//        transaction.setPayCode(payCode);
//        transaction.setType(1); // 充值类型
//        transaction.setStatus(1); // 成功
//        transaction.setRemark("场地充值");
//        transactionDao.insert(transaction);
//        // 4. 更新账户余额
//        accountDao.updateBalance(account.getId(), newBalance, newExtMoney);
//    }
//}