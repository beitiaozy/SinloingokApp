package com.sinloingok.app.service.custom;

import com.sinloingok.app.dao.FundTransactionDao;
import com.sinloingok.app.dao.UserAddressAccountDao;
import com.sinloingok.app.dao.status.GenericRecharge;
import com.sinloingok.app.dao.status.TransactionFlowType;
import com.sinloingok.app.dtos.UserAccountDto;
import com.sinloingok.app.models.fund.ConsumeData;
import com.sinloingok.app.models.fund.FundTransaction;
import com.sinloingok.app.models.fund.UserAddressAccount;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.models.net4g.NetSiteOrder;
import com.sinloingok.app.models.user.RechargeOrder;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.service.custom.strategy.ConsumptionContext;
import com.sinloingok.app.service.custom.strategy.RechargeStrategy;
import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.service.user.UserService;
import com.sinloingok.app.util.FundTransactionUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsumptionService {
    @Autowired
    private UserAddressAccountDao accountDao;
    @Autowired
    private FundUsageRuleService ruleService;

    @Autowired
    private NetSiteService netSiteService;
    @Autowired
    private FundTransactionDao transactionDao;
    @Autowired
    private UserService userService;
    @Autowired
    private List<RechargeStrategy> strategies;

    /**
     * 充值流程：创建账户 + 记录交易 + 更新余额 + 更新总余额
     */
    public void recharge(User user, UserAddressAccount orig, RechargeOrder order) {
        UserAddressAccount account = accountDao.findByUserAndAddress(orig.getUserId(), orig.getAddressId());
        if (account == null) {
            account = new UserAddressAccount(orig.getUserId(), orig.getAddressId(), BigDecimal.ZERO, BigDecimal.ZERO);
            user.setCurrentAccount(account);
            userService.insert(user);
        }else{
            user.setCurrentAccount(account);
        }

        // 选择策略
        RechargeStrategy strategy = resolveStrategy(order.getType());
        ConsumptionContext context = new ConsumptionContext(order, user);
        context.setAttr("newBalance", account.getBalance().add(orig.getBalance()));
        context.setAttr("newExtMoney", account.getExtMoney().add(orig.getExtMoney()));
        context.setAttr("amount", orig.getBalance().add(orig.getExtMoney()));

        strategy.calculate(context);

    }

    /**
     * 消费 + 结算封装（策略 + 扣费规则）
     */
    public List<ConsumeData> consume(NetSiteOrder order, User user, boolean settle) {

        NetSite netSite = netSiteService.selectByOnlyCode(order.getOnlyCode());
        long addressId = netSite.getAddressId();
        // 获取场地优先顺序
        List<Long> sourceAddressIds = ruleService.findSourceAddressesForTarget(addressId);
        if (!sourceAddressIds.contains(addressId)) sourceAddressIds.add(0, addressId);

        List<UserAddressAccount> accounts = accountDao.findByUserAndAddresses(user.getId(), sourceAddressIds);
        BigDecimal remaining = order.getNetAmount();
        List<ConsumeData> result = new ArrayList<>();

        for (UserAddressAccount acc : accounts) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            if (!acc.getAddressId().equals(addressId) && !ruleService.isUsageAllowed(acc.getAddressId(), addressId))
                continue;

            BigDecimal[] deduction = defaultCalculate(acc.getBalance(), acc.getExtMoney(), remaining);

            BigDecimal fromBalance = deduction[0], fromBonus = deduction[1];
            BigDecimal total = fromBalance.add(fromBonus);
            if (total.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal newBalance = acc.getBalance().subtract(fromBalance);
            BigDecimal newExt = acc.getExtMoney().subtract(fromBonus);
            TransactionFlowType consume = TransactionFlowType.CONSUME;
            String message = "洗车消费。设备编号:" + order.getOnlyCode() + " 订单编号：" + order.getPayCode() + " 订单金额：" + order.getTotalAmount() + " 实付金额：" + order.getNetAmount();
            if(StringUtils.isNotEmpty(order.getCalc())){
                message += " " + order.getCalc();
            }
            FundTransaction tx = FundTransactionUtil.build(
                    user.getId(), addressId, order.getPayCode(),
                    acc.getBalance(), newBalance, acc.getExtMoney(), newExt,
                    total.negate(), consume.getCode(),"CAR_WASH_CUMSUME" , message);
            result.add(new ConsumeData(new UserAddressAccount(acc.getId(), newBalance, newExt), tx));
            remaining = remaining.subtract(total);
        }

        if (settle) {
            for (ConsumeData data : result) {
                transactionDao.insert(data.getTransaction()); // 若 insert 抛异常，则整体回滚
                UserAddressAccount acc = data.getAccount();
                accountDao.updateBalance(acc.getId(), acc.getBalance(), acc.getExtMoney());
            }
            updateUserTotalBalance(user);
        }
        return result;
    }

    public void updateBalance(UserAccountDto dto) {
        User user = userService.findById(dto.getUserId(), dto.getAddressId());
        UserAddressAccount acc = accountDao.findByUserAndAddress(dto.getUserId(), dto.getAddressId());
        accountDao.updateBalance(acc.getId(), dto.getBalance(), dto.getExtMoney());
        TransactionFlowType type = TransactionFlowType.ADMIN_MODIFY;
        updateUserTotalBalance(user);
        String message = StringUtils.isEmpty(dto.getRemark()) ? type.getDescription() : dto.getRemark();
        FundTransactionUtil.create(
                dto.getUserId(), dto.getAddressId(), null,
                acc.getBalance(), dto.getBalance(),
                acc.getExtMoney(), dto.getExtMoney(),
                dto.getBalance().subtract(acc.getBalance()), type.getCode(), "REPAIR", message);
    }

    public void updateUserTotalBalance(User user) {
        BigDecimal balance = accountDao.sumBalanceByUser(user.getId());
        BigDecimal ext = accountDao.sumExtMoneyByUser(user.getId());
        userService.updateMoney(user.getId(), balance.floatValue(), ext.floatValue());
    }

    private BigDecimal[] defaultCalculate(BigDecimal balance, BigDecimal ext, BigDecimal amount) {
        BigDecimal fromBalance = balance.min(amount);
        BigDecimal fromExt = BigDecimal.ZERO;
        if (amount.subtract(fromBalance).compareTo(BigDecimal.ZERO) > 0) {
            fromExt = ext.min(amount.subtract(fromBalance));
        }
        return new BigDecimal[]{fromBalance, fromExt};
    }

    private RechargeStrategy resolveStrategy(String type) {
        return strategies.stream()
                .filter(s -> s.support(type))
                .findFirst()
                .orElse(null);
    }
}
