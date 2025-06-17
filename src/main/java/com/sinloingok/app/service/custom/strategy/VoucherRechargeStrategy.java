package com.sinloingok.app.service.custom.strategy;

import com.sinloingok.app.dao.FundTransactionDao;
import com.sinloingok.app.dao.status.GenericRecharge;
import com.sinloingok.app.dao.status.TransactionFlowType;
import com.sinloingok.app.models.bluetooth.BluetoothAddressRecharge;
import com.sinloingok.app.models.fund.FundTransaction;
import com.sinloingok.app.service.netsite.BluetoothAddressRechargeService;
import com.sinloingok.app.service.user.UserVoucherService;
import com.sinloingok.app.util.FundTransactionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class VoucherRechargeStrategy implements RechargeStrategy {

    @Autowired
    private UserVoucherService voucherService;
    @Autowired
    private BluetoothAddressRechargeService rechargeService;

    @Override
    public boolean support(String type) {
        return GenericRecharge.isComboRechargeStrategy(type.toUpperCase());
    }

    @Override
    public boolean shouldSkip(ConsumptionContext ctx) {
        // 若本月已消费过，则跳过扣费
        return true;
    }

    @Override
    public BigDecimal[] calculate(ConsumptionContext ctx) {

        BluetoothAddressRecharge recharge = rechargeService.findRechargeById(ctx.getOrder().getRechargeId());
        voucherService.createVoucher(recharge, ctx.getUser().getId(), ctx.getOrder().getCode());

        GenericRecharge rechargeType = GenericRecharge.fromCode(recharge.getType());

        TransactionFlowType type = rechargeType == GenericRecharge.MONTH_COMBO ? TransactionFlowType.MONTHLY_CARD_RECHARGE : TransactionFlowType.TIMES_CARD_RECHARGE;
        String message = "用户" + ctx.getUser().getMobile() + "充值" + type.getDescription();

        FundTransactionUtil.create(
                ctx.getUser().getId(), recharge.getAddressId(), ctx.getOrder().getCode(),
                ctx.getUser().getCurrentAccount().getBalance(), ctx.getUser().getCurrentAccount().getBalance(),
                ctx.getUser().getCurrentAccount().getExtMoney(), ctx.getUser().getCurrentAccount().getExtMoney(),
                new BigDecimal(recharge.getMoney()), type.getCode(), rechargeType.getCode(), message);

        return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO}; // 不扣费
    }
}
