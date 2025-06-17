package com.sinloingok.app.service.order;

import com.sinloingok.app.config.SBeanUtils;
import com.sinloingok.app.constant.SysConstant;
import com.sinloingok.app.dao.NetSiteOrderDao;
import com.sinloingok.app.dao.status.OrderStatus;
import com.sinloingok.app.models.NetSiteCache;
import com.sinloingok.app.models.fund.ConsumeData;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.models.net4g.NetSiteOrder;
import com.sinloingok.app.models.net4g.NetSiteOrderItemized;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.models.voucher.UserVoucher;
import com.sinloingok.app.service.custom.ConsumptionService;
import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.service.user.UserService;
import com.sinloingok.app.service.user.UserVoucherService;
import com.sinloingok.app.util.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderSettlementService {
    @Autowired
    private UserService userService;
    @Autowired
    private UserVoucherService userVoucherService;
    @Autowired
    private NetSiteOrderDao netSiteOrderDao;
    @Autowired
    private ConsumptionService consumptionService;

    /**
     * 正式结算：修改订单、用户余额等
     */
    public float settleOrder(NetSiteOrder order) {
        User user = userService.findById(order.getUserId(), SysConstant.DEFAULT_ADDRESS);
        // user 对象中的余额及赠额已更新未入库
        consumptionService.consume(order, user, true);
        order.setEndTime(DateUtils.curTime());
        order.setStatus(OrderStatus.FINISHED);
        netSiteOrderDao.update(order);
        UserVoucher voucher = userVoucherService.findById(order.getVoucherId());
        if (voucher != null) {
            voucher.minusAmount();
            userVoucherService.update(voucher);
        }
        return user.getMoney();
    }

    /**
     * 计算订单的消费总额（含低消/代金券等逻辑）
     */
    public boolean calculateRemainingBalance(NetSiteOrder order) {
        if (order == null) return false;

        if (CollectionUtils.isEmpty(order.getItems())) {
            NetSiteOrderItemizedService orderItemizedService = SBeanUtils.getBean(NetSiteOrderItemizedService.class);
            order.setItems(orderItemizedService.selectByPayCode(order.getPayCode()));
        }
        // 獲取基礎消費，每半小時x元
        NetSite netSite = NetSiteCache.wscByOnlyCode(order.getOnlyCode());
        BigDecimal basePrice = roundToTwoDecimal(order, netSite);
        calculateOrderCost(order, basePrice);

        UserVoucher voucher = userVoucherService.findById(order.getVoucherId());
        if (voucher != null) {
            voucher.minusAmount();
            order.setDiscountAmount(new BigDecimal(voucher.getTotalMoney()));
        }

        User user = userService.findById(order.getUserId(), netSite.getAddressId());
        // 计算用户余额是否够实付金额
        List<ConsumeData> result = consumptionService.consume(order, user, false);
        return result.stream()
                .map(ConsumeData::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(order.getNetAmount()) > 0;
    }

    /**
     * 计算 訂單各項消費明細的金額總和 不扣除優惠券部分, 與基礎消費的最大值作對比。
     */
    private void calculateOrderCost(NetSiteOrder order, BigDecimal basePrice) {
        BigDecimal totalAmount = new BigDecimal(0);
        for (NetSiteOrderItemized itemized : order.getItems()) {
            totalAmount = totalAmount.add(itemized.calculateItemAmount());
        }
        order.setTotalAmount(totalAmount.max(basePrice));
    }

    /**
     * 計算該訂單的最低消費門檻
     *
     * @param order
     * @return
     */
    private BigDecimal roundToTwoDecimal(NetSiteOrder order, NetSite netSite) {
        String current = DateUtils.curTime();
        long seconds = DateUtils.secondsDifference(current, order.getBeginTime());
        if (seconds < 120) return new BigDecimal(0);
        return netSite.getBaseUnitAmount().multiply(new BigDecimal(seconds / (30 * 60)).add(new BigDecimal(1)));
    }

}
