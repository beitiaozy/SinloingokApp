package com.sinloingok.app.service.gateway;

import com.alibaba.fastjson.JSONObject;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.models.payment.PaymentAccount;
import com.sinloingok.app.models.payment.TerminalCredential;

import java.util.List;

/**
 * 收钱吧网关（含：完善的 activate / checkin 自动落库）
 */
// com.sinloingok.app.service.payment.gateway.PaymentGateway
public interface PaymentGateway {
    void activate(PaymentAccount account, String activationCode, List<NetSite> sites);

    void checkin(PaymentAccount account, List<TerminalCredential> terminals);

    JSONObject precreate(PaymentAccount account, TerminalCredential terminal, JSONObject requestBody);

    JSONObject query(PaymentAccount account, TerminalCredential terminal, String sn, String clientSn);

    JSONObject refund(PaymentAccount account, TerminalCredential terminal, JSONObject params);

    boolean verifySignature(PaymentAccount account, String rawBody, String authorizationHeader);
}
