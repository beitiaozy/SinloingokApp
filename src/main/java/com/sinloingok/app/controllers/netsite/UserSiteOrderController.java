package com.sinloingok.app.controllers.netsite;

import com.sinloingok.app.aop.AuthCheck;
import com.sinloingok.app.aop.LoginType;
import com.sinloingok.app.aop.UserContext;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.controllers.base.PageData;
import com.sinloingok.app.dao.status.NetSiteStatus;
import com.sinloingok.app.dao.status.OrderStatus;
import com.sinloingok.app.dtos.SimplePageDto;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.dtos.order.NetSiteOrderResDto;
import com.sinloingok.app.dtos.order.NetSiteUserReqDto;
import com.sinloingok.app.models.NetSiteCache;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.models.net4g.NetSiteOrder;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.service.order.NetSiteOrderService;
import com.sinloingok.app.service.order.OrderFulfillmentService;
import com.sinloingok.app.service.order.OrderSettlementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 客户设备订单相关
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@RestController
@RequestMapping("userSiteOrder")
public class UserSiteOrderController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(UserSiteOrderController.class);

    @Autowired
    private OrderFulfillmentService orderFulfillmentService;
    @Autowired
    private NetSiteOrderService netSiteOrderService;
    @Autowired
    private OrderSettlementService settlementService;


    /**
     * 查询当前使用中的订单
     */
    @RequestMapping("getCurrentOrder")
    @AuthCheck(LoginType.USER)
    public StandardRtnDto<?> getCurrentOrder() {
        User user = UserContext.getUser();
        NetSiteOrder order = netSiteOrderService.getCurrentOrder(user.getId(), OrderStatus.USEING);
        if (order != null) {
            settlementService.calculateRemainingBalance(order);
        }
        return success(order);
    }

    /**
     * 余额支付扫码开门 生成订单
     */
    @RequestMapping("beginOrder")
    @AuthCheck(LoginType.USER)
    public StandardRtnDto<?> beginOrder(@RequestBody Map<String, String> params) {
        User user = UserContext.getUser();
        String dev_id = params.get("dev_id");
        long voucher_id = Long.valueOf(params.getOrDefault("voucher_id", "-1"));
        try {
            NetSite netSite = NetSiteCache.wscByOnlyCode(dev_id);
            if (netSite == null && netSite.getStatus().equals(NetSiteStatus.OFFLINE)) {
                return error("设备信息错误或设备离线");
            }
            // 判断设备是否有空闲
            if (netSite.getStatus().equals(OrderStatus.USEING)) {
                return error("设备正在使用中 请耐心等待");
            }

            NetSiteOrder current = netSiteOrderService.getCurrentOrder(user.getId(), OrderStatus.USEING);
            if (current != null) {
                return error("有未完成的订单");
            }

            Float money = user.getMoney();
            if (voucher_id == -1 && money <= 3) {
                return error("账户余额不足3元 请先充值");
            }
            orderFulfillmentService.createOrderAndActivateDevice(netSite, user.getId(), voucher_id);

            return success("订单开始");
        } catch (Exception e) {
            logger.error("订单开始系统错误", e);
//            DbKit.getThreadLocalConnection().rollback();
            return error("订单开始系统错误");
        }
    }


    /**
     * 我的订单
     */
    @RequestMapping("orderList")
    @AuthCheck(LoginType.USER)
    public StandardRtnDto<?> queryNetSiteOrderByPage(@RequestBody PageData<NetSiteUserReqDto> pageData) {
        long userID = UserContext.getUser().getId();
        if (pageData.getBody() != null) {
            pageData.getBody().setUserId(userID);
        }
        SimplePageDto<NetSiteOrderResDto> result = netSiteOrderService.queryNetSiteOrderByPage(pageData);
        return success(result);
    }


    /**
     * 结束订单
     */
    @RequestMapping("endOrder")
    @AuthCheck(LoginType.USER)
    public StandardRtnDto<?> endOrder(@RequestBody Map<String, String> params) {
        String payCode = params.getOrDefault("pay_code", "-1");
        try {
            if (!netSiteOrderService.hasNetSiteOrderStatusExists(payCode, OrderStatus.USEING)) {
                return error("只有使用中的订单可以进行取消操作");
            }
            // 计算费用并且自动扣除余额
            orderFulfillmentService.completeOrderAndReleaseDevice(payCode);
            return success("结束订单成功");
        } catch (Exception e) {
            logger.error("取消订单系统错误", e);
            return error("取消订单系统错误");
        }
    }
}
