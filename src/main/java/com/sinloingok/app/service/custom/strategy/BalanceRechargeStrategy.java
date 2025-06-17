package com.sinloingok.app.service.custom.strategy;

import com.sinloingok.app.dao.FundTransactionDao;
import com.sinloingok.app.dao.status.GenericRecharge;
import com.sinloingok.app.dao.status.TransactionFlowType;
import com.sinloingok.app.models.fund.FundTransaction;
import com.sinloingok.app.models.fund.UserAddressAccount;
import com.sinloingok.app.models.user.RechargeOrder;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.service.user.UserService;
import com.sinloingok.app.util.FundTransactionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class BalanceRechargeStrategy implements RechargeStrategy {
    @Autowired
    private UserService userService;

    @Override
    public boolean support(String type) {
        return GenericRecharge.isBalanceRechargeStrategy(type);
    }

    @Override
    public boolean shouldSkip(ConsumptionContext ctx) {
        // 若本月已消费过，则跳过扣费
        return true;
    }

    @Override
    public BigDecimal[] calculate(ConsumptionContext ctx) {
        User user = ctx.getUser();
        UserAddressAccount account = user.getCurrentAccount();
        RechargeOrder order = ctx.getOrder();

        BigDecimal newBalance = ctx.getAttr("newBalance", BigDecimal.class);
        BigDecimal newExtMoney = ctx.getAttr("newExtMoney", BigDecimal.class);
        BigDecimal beforeBalance = account.getBalance();
        BigDecimal beforeExtMoney = account.getExtMoney();
        BigDecimal amount = ctx.getAttr("amount", BigDecimal.class);

        account.setBalance(newBalance);
        account.setExtMoney(newExtMoney);
        userService.updateUserTotalBalance(user);
        TransactionFlowType type = TransactionFlowType.RECHARGE;
        GenericRecharge rechargeType = GenericRecharge.fromCode(order.getType());


        String message = "用户" + ctx.getUser().getMobile() + "充值" + type.getDescription();
        FundTransactionUtil.create(user.getId(), account.getAddressId(), order.getCode(),
                beforeBalance, newBalance, beforeExtMoney, newExtMoney, amount, type.getCode(), rechargeType.getCode(), message
        );
        return new BigDecimal[]{newBalance, newExtMoney}; // 不扣费
    }
}
