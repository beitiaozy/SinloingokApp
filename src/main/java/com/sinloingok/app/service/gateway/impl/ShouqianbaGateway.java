package com.sinloingok.app.service.gateway.impl;

import com.alibaba.fastjson.JSONObject;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.models.payment.PaymentAccount;
import com.sinloingok.app.models.payment.TerminalCredential;
import com.sinloingok.app.service.gateway.PaymentGateway;
import com.sinloingok.app.service.payment.TerminalCredentialProvider;
import com.sinloingok.app.util.HttpUtil;
import com.sinloingok.app.util.pay.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

// com.sinloingok.app.service.payment.gateway.impl.ShouqianbaGateway
@Service
public class ShouqianbaGateway implements PaymentGateway {

    @Autowired
    private TerminalCredentialProvider terminalProvider;

    // ====== 激活：为每个 NetSite 申请 / 更新 终端 ======
    @Override
    public void activate(PaymentAccount account, String activationCode, List<NetSite> sites) {
        String url = account.getApiUrl() + "/terminal/activate";
        for (NetSite ns : sites) {
            JSONObject req = new JSONObject();
            req.put("vendor_sn", account.getVendorSn());
            req.put("vendor_key", account.getVendorKey());
            req.put("app_id", account.getAppId());
            req.put("device_id", "sln" + ns.getOnlyCode()); // 你的规则
            req.put("code", activationCode);

            String sign = SignUtil.sign(req.toJSONString(), account.getVendorKey());
            String authorization = account.getVendorSn() + " " + sign;

            String resp = HttpUtil.postJson(url, req.toJSONString(), authorization);
            JSONObject root = JSONObject.parseObject(resp);

            // 收钱吧标准响应一般含 result_code & biz_response
            // 兼容两种：顶层 or biz_response.data
            String terminalSn = null;
            String terminalKey = null;

            if (root.containsKey("result_code") && "200".equals(String.valueOf(root.get("result_code")))) {
                JSONObject biz = root.getJSONObject("biz_response");
                if (biz != null) {
                    JSONObject data = biz.getJSONObject("data");
                    if (data != null) {
                        terminalSn = data.getString("terminal_sn");
                        terminalKey = data.getString("terminal_key");
                    }
                }
            } else {
                // 有些 SDK/代理网关会直接把终端号放在顶层
                terminalSn = root.getString("terminal_sn");
                terminalKey = root.getString("terminal_key");
            }

            if (terminalSn != null && terminalKey != null) {
                // 落库：site + account 维度
                TerminalCredential exist = terminalProvider.getBySiteAndAccount(ns.getId(), account.getId());
                if (exist == null) {
                    TerminalCredential tc = new TerminalCredential();
                    tc.setAddressId(ns.getAddressId());
                    tc.setPaymentAccountId(account.getId());
                    tc.setTerminalSn(terminalSn);
                    tc.setTerminalKey(terminalKey);
                    tc.setIsEnabled(1);
                    terminalProvider.saveOrUpdate(tc);
                } else {
                    exist.setTerminalSn(terminalSn);
                    exist.setTerminalKey(terminalKey);
                    exist.setIsEnabled(1);
                    terminalProvider.saveOrUpdate(exist);
                }
            } else {
                // 记录错误信息（可根据 root 打印 detail）
                // log.warn("Activate failed for site={}, resp={}", ns.getId(), resp);
            }
        }
    }

    // ====== 签到：刷新每个终端的 sn/key（有时会下发新 key） ======
    @Override
    public void checkin(PaymentAccount account, List<TerminalCredential> terminals) {
        String url = account.getApiUrl() + "/terminal/checkin";
        for (TerminalCredential tc : terminals) {
            JSONObject req = new JSONObject();
            req.put("terminal_sn", tc.getTerminalSn());
            req.put("device_id", tc.getAddressId()); // 也可以用 only_code

            String sign = SignUtil.sign(req.toJSONString(), tc.getTerminalKey());
            String authorization = tc.getTerminalSn() + " " + sign;

            String resp = HttpUtil.postJson(url, req.toJSONString(), authorization);
            JSONObject root = JSONObject.parseObject(resp);

            String newSn = null;
            String newKey = null;
            if (root.containsKey("result_code") && "200".equals(String.valueOf(root.get("result_code")))) {
                JSONObject biz = root.getJSONObject("biz_response");
                if (biz != null) {
                    JSONObject data = biz.getJSONObject("data");
                    if (data != null) {
                        newSn = data.getString("terminal_sn");
                        newKey = data.getString("terminal_key");
                    }
                }
            } else {
                newSn = root.getString("terminal_sn");
                newKey = root.getString("terminal_key");
            }

            if (newSn != null && newKey != null) {
                tc.setTerminalSn(newSn);
                tc.setTerminalKey(newKey);
                terminalProvider.saveOrUpdate(tc);
            } else {
                // log.warn("Checkin failed for terminal={}, resp={}", tc.getTerminalSn(), resp);
            }
        }
    }

    // ====== 统一交易接口 ======
    @Override
    public JSONObject precreate(PaymentAccount account, TerminalCredential terminal, JSONObject requestBody) {
        String url = account.getApiUrl() + "/upay/v2/precreate";
        requestBody.put("terminal_sn", terminal.getTerminalSn());
        String sign = SignUtil.sign(requestBody.toJSONString(), terminal.getTerminalKey());
        String authorization = terminal.getTerminalSn() + " " + sign;
        String resp = HttpUtil.postJson(url, requestBody.toJSONString(), authorization);
        return JSONObject.parseObject(resp);
    }

    @Override
    public JSONObject query(PaymentAccount account, TerminalCredential terminal, String sn, String clientSn) {
        String url = account.getApiUrl() + "/upay/v2/query";
        JSONObject params = new JSONObject();
        params.put("terminal_sn", terminal.getTerminalSn());
        params.put("sn", sn);
        params.put("client_sn", clientSn);
        String sign = SignUtil.sign(params.toJSONString(), terminal.getTerminalKey());
        String authorization = terminal.getTerminalSn() + " " + sign;
        String resp = HttpUtil.postJson(url, params.toJSONString(), authorization);
        return JSONObject.parseObject(resp);
    }

    @Override
    public JSONObject refund(PaymentAccount account, TerminalCredential terminal, JSONObject params) {
        String url = account.getApiUrl() + "/upay/v2/refund";
        params.put("terminal_sn", terminal.getTerminalSn());
        String sign = SignUtil.sign(params.toJSONString(), terminal.getTerminalKey());
        String authorization = terminal.getTerminalSn() + " " + sign;
        String resp = HttpUtil.postJson(url, params.toJSONString(), authorization);
        return JSONObject.parseObject(resp);
    }

    // ====== 验签（用平台公钥 + 原始请求体） ======
    @Override
    public boolean verifySignature(PaymentAccount account, String rawBody, String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) return false;
        try {
            String[] parts = authorizationHeader.split("\\s+");
            if (parts.length != 2) return false;
            String signBase64 = parts[1];

            java.security.Signature signature = java.security.Signature.getInstance("SHA256withRSA");
            PublicKey pub = loadRsaPublicKey(account.getPlatformPublicKey());
            signature.initVerify(pub);
            signature.update(rawBody.getBytes(StandardCharsets.UTF_8));
            byte[] sigBytes = org.bouncycastle.util.encoders.Base64.decode(signBase64);
            return signature.verify(sigBytes);
        } catch (Exception e) {
            // log.warn("verifySignature error", e);
            return false;
        }
    }

    private PublicKey loadRsaPublicKey(String base64) throws Exception {
        byte[] keyBytes = org.bouncycastle.util.encoders.Base64.decode(base64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
