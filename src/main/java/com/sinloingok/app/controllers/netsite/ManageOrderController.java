package com.sinloingok.app.controllers.netsite;

import com.sinloingok.app.aop.AuthCheck;
import com.sinloingok.app.aop.LoginType;
import com.sinloingok.app.aop.UserContext;
import com.sinloingok.app.constant.SinloingokUserStatus;
import com.sinloingok.app.controllers.base.PageData;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.dtos.order.NetSiteOrderResDto;
import com.sinloingok.app.dtos.order.NetSiteUserReqDto;
import com.sinloingok.app.service.order.OrderFulfillmentService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.dtos.SimplePageDto;
import com.sinloingok.app.service.order.NetSiteOrderService;
import com.sinloingok.app.models.user.ManageUser;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * 员工订单
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@RestController
@RequestMapping("manageOrder")
public class ManageOrderController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ManageOrderController.class);

    @Autowired
    private NetSiteOrderService netSiteOrderService;

    @Autowired
    private OrderFulfillmentService orderFulfillmentService;

    /**
     * 设备订单列表
     */
    @RequestMapping("orderList")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<?> orderList(@RequestBody PageData<NetSiteUserReqDto> pageData) {
        try {
            SimplePageDto<NetSiteOrderResDto> result = netSiteOrderService.queryNetSiteOrderByPage(pageData);
            return success(result);
        } catch (Exception e) {
            logger.error("设备订单列表系统错误", e);
            return error("设备订单列表系统错误");
        }
    }

    /**
     * 取消订单
     */
    @RequestMapping("cancel")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<String> cancel(@RequestBody Map<String, String> params) {
        String payCode = params.getOrDefault("pay_code", "-1");
        ManageUser manageUser = UserContext.getMUser();
        if (manageUser.getStatus().equals(SinloingokUserStatus.ManageUserStatus.NO_USING)) {
            return error("账号异常！");
        }
        try {
            orderFulfillmentService.completeOrderAndReleaseDevice(payCode);
            return success("取消订单成功");
        } catch (Exception e) {
            logger.error("取消订单系统错误", e);
            return error("取消订单系统错误");
        }
    }
}
