package com.sinloingok.app.controllers.payment;

import com.alibaba.fastjson.JSONObject;
import com.sinloingok.app.aop.AuthCheck;
import com.sinloingok.app.aop.LoginType;
import com.sinloingok.app.aop.UserContext;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.service.payment.PaymentFacade;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/slg/payment")
public class SlnPaymentController extends BaseController {

    private final PaymentFacade paymentFacade;

    public SlnPaymentController(PaymentFacade paymentFacade) {
        this.paymentFacade = paymentFacade;
    }

    @RequestMapping("/activate")
    public StandardRtnDto<String> activate(@RequestBody Map<String, String> params) {
        // 激活改到后台统一批量处理（可扩展：传入accountId/activationCode，再由Facade调 Gateway.activate）
        // 这里只给个成功提示或触发后台任务
        return success("激活任务已触发");
    }

    @RequestMapping("checkin")
    public StandardRtnDto<String> checkin() {
        // 同上：Facade 内部遍历所有启用账户/终端执行
        return success("签到任务已触发");
    }

    @RequestMapping("/query")
    public StandardRtnDto<?> query(@RequestBody Map<String, String> params) {
        long orderId = Long.parseLong(params.getOrDefault("order_id", "-1"));
        JSONObject result = paymentFacade.query(orderId);
        return success(result);
    }

    @RequestMapping("refund")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<?> refund(@RequestBody Map<String, String> params) {
        BigDecimal refundAmt = new BigDecimal(params.getOrDefault("refund_amt", "-1"));
        long userId = Long.parseLong(params.getOrDefault("user_id", "-1"));
        JSONObject refundRes = paymentFacade.refund(userId, refundAmt);
        return success(refundRes);
    }

    @RequestMapping("getCodeForRechargePay")
    @AuthCheck(LoginType.USER)
    public StandardRtnDto<?> getCodeForRechargePay(@RequestBody Map<String, String> params) {
        long orderId = Long.parseLong(params.getOrDefault("order_id", "-1"));
        Long userId = UserContext.getUser().getId();
        JSONObject result = paymentFacade.preCreateRecharge(orderId, userId);
        if (result != null && result.containsKey("biz_response")) {
            return success(result.getJSONObject("biz_response").get("data"));
        }
        return error("支付失败，请联系管理员");
    }

    @RequestMapping("sbpaycallForRecharge")
    public String sbpaycallForRecharge(HttpServletRequest request, HttpServletResponse response) {
        try {
            String raw = readBody(request); // 读取原始 JSON
            boolean ok = paymentFacade.handleRechargeCallback(request, raw);
            if (!ok) return "fail";
        } catch (Exception e) {
            return "fail";
        }
        return "success";
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try(BufferedReader r = request.getReader()) {
            String line; while((line = r.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }
}
