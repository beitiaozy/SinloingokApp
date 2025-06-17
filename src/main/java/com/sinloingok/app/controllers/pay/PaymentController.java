package com.sinloingok.app.controllers.pay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sinloingok.app.aop.AuthCheck;
import com.sinloingok.app.aop.LoginType;
import com.sinloingok.app.aop.UserContext;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.models.fund.UserAddressAccount;
import com.sinloingok.app.models.user.RechargeOrder;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.models.user.UserMoneyRecord;
import com.sinloingok.app.service.custom.ConsumptionService;
import com.sinloingok.app.service.pay.PaymentService;
import com.sinloingok.app.service.user.RechargeOrderService;
import com.sinloingok.app.service.user.UserMoneyRecordService;
import com.sinloingok.app.service.user.UserService;
import com.sinloingok.app.util.PropertiesUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

/**
 * 收钱吧支付控制类
 */
@RestController
@RequestMapping("/payment")
public class PaymentController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final static String AppID = PropertiesUtils.getProperties().getProperty("WEIXIN_APPID");// 开发者ID
    @Value("${sinloingok.wechat.notifyUrlRecharge}")
    private String notify_url_recharge;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RechargeOrderService rechargeOrderService;

    @Autowired
    private UserMoneyRecordService userMoneyRecordService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConsumptionService consumptionService;

    @RequestMapping("/activate")
    public StandardRtnDto<String> activate(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        if (StringUtils.isNotEmpty(code)) {
            paymentService.activate(code);
        }
        return success("激活成功");
    }


    @RequestMapping("checkin")
    public StandardRtnDto<String> checkin() {
        paymentService.checkin();
        return success("激活成功");
    }

    @RequestMapping("/query")
    public StandardRtnDto<?> query(@RequestBody Map<String, String> params) {
        long order_id = Long.valueOf(params.getOrDefault("order_id", "-1"));
        String[] orderParams = queryOrderParam(order_id);
        if (orderParams != null && orderParams.length == 2) {
            JSONObject result = paymentService.query(orderParams[0], orderParams[1]);
            return success(result);
        }
        return error("查询失败");
    }

    @RequestMapping("refund")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto refund(@RequestBody Map<String, String> paramss) {
        float refundAmt = Float.valueOf(paramss.getOrDefault("refund_amt", "-1"));
        long user_id = Long.valueOf(paramss.getOrDefault("user_id", "-1"));
        RechargeOrder order = rechargeOrderService.findLatestByUserAndStatus(user_id, "已充值");
        if (order == null) {
            return error("该用户无充值记录，退款拒绝");
        }
        float moneyNumber = order.getMoney();
        if (moneyNumber < refundAmt) {
            return error("退款额度大于最近一笔充值余额，退款拒绝");
        }

        float multiplied = refundAmt * 100;
        int integerPart = Math.round(multiplied); //进行四舍五入
        String amount = Integer.toString(integerPart);
        JSONObject params = new JSONObject();
        String[] paramSn = queryOrderParam(order.getId());
        if (paramSn != null && paramSn.length == 2) {
            float res = (moneyNumber - refundAmt);
            // 退款发生时，需要清理赠送额度 --退款业务用于全额退款，用户默认不在使用该平台，未保留记录，用负数处理
            order.setMoney(res);
            rechargeOrderService.update(order);

            params.put("sn", paramSn[0]);              //收钱吧系统内部唯一订单号
            params.put("client_sn", paramSn[1]);   //商户系统订单号,必须在商户系统内唯一；且长度不超过64字节
            params.put("refund_amount", amount);               //退款金额
            params.put("refund_request_no", order.getCode());          //商户退款所需序列号,表明是第几次退款
            params.put("operator", "係統用戶");                      //门店操作员
            JSONObject refundRes = paymentService.refund(params);
            return success(refundRes);
        } else {
            return error("该用户没有通过收钱吧充值，请联系客服线下处理！");
        }
    }

    /**
     * 查询收钱吧订单参数
     *
     * @return
     */
    private String[] queryOrderParam(Long order_id) {
        UserMoneyRecord record = userMoneyRecordService.findByMinOrderId(order_id);
        if (record != null) {
            String msgStr = record.getMessage();
            if (msgStr != null) {
                JSONObject msg = JSONObject.parseObject(msgStr);
                String sn = msg.getString("sn");
                String clientSN = msg.getString("client_sn");
                if (StringUtils.isNotEmpty(sn) && StringUtils.isNotEmpty(clientSN)) {
                    return new String[]{sn, clientSN};
                }
            }
        }
        return null;
    }

    /**
     * 获取充值支付参数
     */
    @RequestMapping("getCodeForRechargePay")
    @AuthCheck(LoginType.USER)
    public StandardRtnDto<?> getCodeForRechargePay(@RequestBody Map<String, String> params) {
        long orderId = Long.valueOf(params.getOrDefault("order_id", "-1"));
        User user = UserContext.getUser();
        try {
            // 订单
            RechargeOrder order = rechargeOrderService.findById(orderId);
            if (!order.getStatus().equals("未充值")) {
                return error("订单状态不为待支付,发起支付失败");
            }

            int moneyNumber = (int) (order.getMoney() * 100);

            String userName = user.getUsername();
            JSONObject requestBody = new JSONObject();
            requestBody.put("notify_url", notify_url_recharge);
            requestBody.put("payer_uid", user.getOpenId());
            requestBody.put("total_amount", String.valueOf(moneyNumber)); // 分为单位（56元）
            requestBody.put("subject", "用户 " + userName + " 充值:" + order.getMoney());
            requestBody.put("client_sn", order.getCode());
            requestBody.put("payway", "3");// 小程序支付方式
            requestBody.put("sub_payway", "4");
            // 小程序appid（这里是必填）
            JSONObject extended = new JSONObject();
            extended.put("sub_appid", AppID);
            requestBody.put("extended", extended);
            JSONObject result = paymentService.precreate(requestBody);
            if (result != null && result.containsKey("biz_response")) {
                return success(result.getJSONObject("biz_response").get("data"));
            } else {
                return error("支付失败，请联系管理员");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return error("获取支付参数失败");
        }
    }


    /**
     * 收钱吧回调(充值余额)
     */
    @RequestMapping("sbpaycallForRecharge")
    public String sbpaycallForRecharge(HttpServletRequest request, HttpServletResponse response) {
        String returncode = "FAIL";
        String returnMsg = "签名校验失败";
        try {
            // Step 1: 读取请求体
            StringBuilder jsonBuilder = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            String requestBody = jsonBuilder.toString();
            System.out.println("接收到的回调JSON: " + requestBody);

            // Step 2: 解析JSON
            JSONObject notifyData = JSON.parseObject(requestBody);

            // Step 3: 获取Authorization
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null || authorizationHeader.isEmpty()) {
                System.out.println("Authorization为空，签名验证失败");
                error("Authorization为空，签名验证失败"); // 签名验证失败
                return "fail";
            }
            String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5+MNqcjgw4bsSWhJfw2M+gQB7P+pEiYOfvRmA6kt7Wisp0J3JbOtsLXGnErn5ZY2D8KkSAHtMYbeddphFZQJzUbiaDi75GUAG9XS3MfoKAhvNkK15VcCd8hFgNYCZdwEjZrvx6Zu1B7c29S64LQPHceS0nyXF8DwMIVRcIWKy02cexgX0UmUPE0A2sJFoV19ogAHaBIhx5FkTy+eeBJEbU03Do97q5G9IN1O3TssvbYBAzugz+yUPww2LadaKexhJGg+5+ufoDd0+V3oFL0/ebkJvD0uiBzdE3/ci/tANpInHAUDIHoWZCKxhn60f3/3KiR8xuj2vASgEqphxT5OfwIDAQAB";

            // Step 4: 计算签名
            if (validateSign(requestBody + requestBody, authorizationHeader, publicKey)) {
                error("签名验证失败"); // 签名验证失败
                return "fail";
            }
//
            String out_trade_no = notifyData.getString("client_sn");

            // 校验微信的通知返回参数
            User user = userService.findById(Long.valueOf(out_trade_no.substring(8)), 4l);
            RechargeOrder order = rechargeOrderService.findByCodeAndUser(out_trade_no,
                    user.getId());
            if (order == null) {
                logger.warn("充值订单不存在");
                returnMsg = "缺少必要参数";
                responseOut(response, buildWxResult(returncode, returnMsg));
                success("");
                return "success";
            }

            if (!"SUCCESS".equals(notifyData.getString("status"))) {
                order.setStatus(notifyData.getString("status"));
                rechargeOrderService.update(order);
                error("充值失败，请联系客服！！");
                return "fail";
            } else {
                System.out.println("支付成功回调记录 : " + notifyData.toString());
            }

            // Step 5: 业务处理
            if (order.getStatus().equals("未充值")) {
                if (user != null) {
                    Float money = order.getMoney();
                    Float awards = order.getAwards();
                    UserAddressAccount account = new UserAddressAccount(user.getId(), 4l, new BigDecimal(money), new BigDecimal(awards));
                    consumptionService.recharge(user, account, order);
                    // 添加平台通用余额
                    // 修改充值订单状态
                    order.setStatus("已充值");
                    rechargeOrderService.update(order);
                    userMoneyRecordService.createUserMoneyRechargeRecord(order, requestBody);
                }
            }
        } catch (Exception e) {
            logger.warn("[sbpaycall], notify params process fail. ", e);
        }
        return "success";
    }

    /**
     * 验签
     *
     * @param data      签名原数据
     * @param sign      签名
     * @param publicKey 收银吧公钥
     */
    private boolean validateSign(String data, String sign, String publicKey) {
        try {
            java.security.Signature signature = java.security.Signature.getInstance("SHA256WithRSA");
            PublicKey localPublicKey = getPublicKeyFromX509("RSA", publicKey);
            signature.initVerify(localPublicKey);
            signature.update(data.getBytes());
            byte[] bytesSign = Base64.decode(sign);
            return signature.verify(bytesSign);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private PublicKey getPublicKeyFromX509(String algorithm, String publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(new
                X509EncodedKeySpec(Base64.decode(publicKey)));
    }


    /*
     * 组建微信支付通知接口返回信息,xml
     */
    private String buildWxResult(String returncode, String returnMsg) {
        String result = null;
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("xml");
        Element return_code = root.addElement("return_code");
        return_code.setText("<![CDATA[" + returncode + "]]>");
        Element return_msg = root.addElement("return_msg");
        return_msg.setText("<![CDATA[" + returnMsg + "]]>");

        result = root.asXML();
        return result;
    }


    private void responseOut(HttpServletResponse response, String result) {
        try {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().print(result);
        } catch (IOException e) {
            logger.warn("[responseOut] 输出结果参数失败", e);
        }
    }

}
