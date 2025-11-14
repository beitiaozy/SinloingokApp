package com.sinloingok.app.service.payment;

import com.sinloingok.app.dao.PaymentAccountMapper;
import com.sinloingok.app.models.NetSiteCache;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.models.payment.PaymentAccount;
import com.sinloingok.app.models.payment.TerminalCredential;
import com.sinloingok.app.models.user.RechargeOrder;
import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.service.user.RechargeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountResolver {
    @Autowired
    private PaymentAccountMapper accountMapper;
    @Autowired
    private RechargeOrderService orderService; // 你已有

    @Autowired
    private TerminalCredentialProvider provider;

    public PaymentAccount resolveByOrderId(Long orderId) {
        RechargeOrder order = orderService.findById(orderId);
        String onlyCode = order.getOnlyCode(); // 若无此字段，按你的表关系补一条反查
        return resolveByNetSiteId(onlyCode);
    }

    public PaymentAccount resolveByNetSiteId(String onlyCode) {
        TerminalCredential tc = provider.getByOnlyCode(onlyCode);
        return accountMapper.selectById(tc.getPaymentAccountId());
    }

    public PaymentAccount resolveForUser(Long userId) {
        List<PaymentAccount> list = accountMapper.listEnabled();
        if (list.isEmpty()) throw new IllegalStateException("未配置可用支付账户");
        return list.get(0);
    }
}
