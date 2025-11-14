package com.sinloingok.app.service.payment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sinloingok.app.dao.PaymentAccountMapper;
import com.sinloingok.app.models.fund.UserAddressAccount;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.models.payment.PaymentAccount;
import com.sinloingok.app.models.payment.TerminalCredential;
import com.sinloingok.app.models.user.RechargeOrder;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.models.user.UserMoneyRecord;
import com.sinloingok.app.service.custom.ConsumptionService;
import com.sinloingok.app.service.gateway.impl.ShouqianbaGateway;
import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.service.user.RechargeOrderService;
import com.sinloingok.app.service.user.UserMoneyRecordService;
import com.sinloingok.app.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

// com.sinloingok.app.service.payment.PaymentFacade
@Service
public class PaymentFacade {

    @Value("${sinloingok.wechat.notifyUrlRecharge}")
    private String notifyUrlRecharge;

    @Autowired
    private AccountResolver accountResolver;
    @Autowired
    private TerminalCredentialProvider terminalProvider;
    @Autowired
    private ShouqianbaGateway shouqianbaGateway;

    @Autowired
    private PaymentAccountMapper paymentAccountMapper;
    @Autowired
    private NetSiteService netSiteService;

    @Autowired
    private RechargeOrderService rechargeOrderService;
    @Autowired
    private UserService userService;
    @Autowired
    private ConsumptionService consumptionService;
    @Autowired
    private UserMoneyRecordService userMoneyRecordService;

    // ========= 激活：对某个账号下的全部启用 NetSite 执行激活 =========
    @Transactional(rollbackFor = Exception.class)
    public void activateAll(Long paymentAccountId, String activationCode) {
        PaymentAccount acct = paymentAccountMapper.selectById(paymentAccountId);
        if (acct == null || acct.getIsEnabled() == 0) throw new IllegalStateException("支付账户无效");

        List<NetSite> sites = netSiteService.listActive(); // 你已有的接口
        // 若一个账户只负责部分场地，可在此处过滤：site.payment_account_id == paymentAccountId
        sites = sites.stream()
                .filter(s -> paymentAccountId.equals(s.getPaymentAccountId()))
                .collect(Collectors.toList());

        shouqianbaGateway.activate(acct, activationCode, sites);
    }

    // ========= 签到：刷新该账号下所有場地 =========
    @Transactional(rollbackFor = Exception.class)
    public void checkinAll(Long paymentAccountId) {
        PaymentAccount acct = paymentAccountMapper.selectById(paymentAccountId);
        if (acct == null || acct.getIsEnabled() == 0) throw new IllegalStateException("支付账户无效");
        List<TerminalCredential> tcs = terminalProvider.listByAccount(paymentAccountId);
        if (tcs == null || tcs.isEmpty()) return;
        shouqianbaGateway.checkin(acct, tcs);
    }

    // ========= 交易 =========
    public JSONObject preCreateRecharge(Long orderId, Long userId) {
        RechargeOrder order = rechargeOrderService.findById(orderId);
        if (!"未充值".equals(order.getStatus())) {
            throw new IllegalStateException("订单状态不为待支付");
        }

        // 1) 解析账户（基于 order -> netSite -> account）
        PaymentAccount acct = accountResolver.resolveByOrderId(orderId);

        // 2) 找终端凭据（基于 netSiteId）
        String netSiteId = order.getOnlyCode(); // 若无此字段，按你的表关系补一条反查
        TerminalCredential tc = terminalProvider.getByOnlyCode(netSiteId);

        // 3) 构造请求体
        User user = userService.findById(userId, 4L);
        JSONObject req = new JSONObject();
        int moneyNumber = (int) (order.getMoney() * 100);
        req.put("notify_url", notifyUrlRecharge);
        req.put("payer_uid", user.getOpenId());
        req.put("total_amount", String.valueOf(moneyNumber));
        req.put("subject", "用户 " + user.getUsername() + " 充值:" + order.getMoney());
        req.put("client_sn", order.getCode());
        req.put("payway", "3");
        req.put("sub_payway", "4");
        JSONObject ext = new JSONObject();
        ext.put("sub_appid", acct.getWechatSubAppId());
        req.put("extended", ext);
        System.out.println(JSONObject.toJSONString(req));
        // 4) 调用网关
        JSONObject result = shouqianbaGateway.precreate(acct, tc, req);
        return result;
    }

    public JSONObject query(Long orderId) {
        PaymentAccount acct = accountResolver.resolveByOrderId(orderId);
        RechargeOrder order = rechargeOrderService.findById(orderId);
        TerminalCredential tc = terminalProvider.getByOnlyCode(order.getOnlyCode());

        String[] snPair = extractSnClientSn(orderId);
        return shouqianbaGateway.query(acct, tc, snPair[0], snPair[1]);
    }

    @Transactional(rollbackFor = Exception.class)
    public JSONObject refund(Long userId, BigDecimal refundAmt) {
        RechargeOrder order = rechargeOrderService.findLatestByUserAndStatus(userId, "已充值");
        if (order == null) throw new IllegalStateException("该用户无充值记录");
        BigDecimal balance = BigDecimal.valueOf(order.getMoney());
        if (balance.compareTo(refundAmt) < 0) throw new IllegalArgumentException("退款额度过大");

        PaymentAccount acct = accountResolver.resolveByOrderId(order.getId());
        TerminalCredential tc = terminalProvider.getByOnlyCode(order.getOnlyCode());

        BigDecimal fen = refundAmt.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
        String[] snPair = extractSnClientSn(order.getId());
        JSONObject params = new JSONObject();
        params.put("sn", snPair[0]);
        params.put("client_sn", snPair[1]);
        params.put("refund_amount", fen.toPlainString());
        params.put("refund_request_no", order.getCode());
        params.put("operator", "系统用户");

        // 预更新本地余额
        order.setMoney(balance.subtract(refundAmt).floatValue());
        rechargeOrderService.update(order);

        return shouqianbaGateway.refund(acct, tc, params);
    }

    public boolean handleRechargeCallback(HttpServletRequest request, String rawBody) {
        JSONObject notify = JSON.parseObject(rawBody);
        String clientSn = notify.getString("client_sn");
        Long orderId = parseOrderIdFromClientSn(clientSn);

        PaymentAccount acct = accountResolver.resolveByOrderId(orderId);
        boolean ok = shouqianbaGateway.verifySignature(acct, rawBody, request.getHeader("Authorization"));
        if (!ok) return false;

        RechargeOrder order = rechargeOrderService.findById(orderId);
        if (!"SUCCESS".equals(notify.getString("status"))) {
            order.setStatus(notify.getString("status"));
            rechargeOrderService.update(order);
            return false;
        }
        if ("未充值".equals(order.getStatus())) {
            User user = userService.findById(order.getUserId(), 4L);
            UserAddressAccount account = new UserAddressAccount(
                    user.getId(), 4L,
                    BigDecimal.valueOf(order.getMoney()),
                    BigDecimal.valueOf(order.getAwards()));
            consumptionService.recharge(user, account, order);
            order.setStatus("已充值");
            rechargeOrderService.update(order);
            userMoneyRecordService.createUserMoneyRechargeRecord(order, rawBody);
        }
        return true;
    }

    private String[] extractSnClientSn(Long orderId) {
        // 你的原逻辑：从 UserMoneyRecord.message(JSON) 取 sn/client_sn
        UserMoneyRecord record = userMoneyRecordService.findByMinOrderId(orderId);
        if (record == null || record.getMessage() == null) throw new IllegalStateException("缺少支付记录");
        JSONObject msg = JSONObject.parseObject(record.getMessage());
        String sn = msg.getString("sn");
        String clientSn = msg.getString("client_sn");
        return new String[]{sn, clientSn};
    }

    private Long parseOrderIdFromClientSn(String clientSn) {
        return Long.valueOf(clientSn.substring(8));
    }
}
