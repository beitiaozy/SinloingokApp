package com.sinloingok.app.service.order;

import com.sinloingok.app.config.SBeanUtils;
import com.sinloingok.app.controllers.base.PageData;
import com.sinloingok.app.dao.NetSiteOrderDao;
import com.sinloingok.app.dao.status.NetSiteStatus;
import com.sinloingok.app.dao.status.OrderStatus;
import com.sinloingok.app.dtos.SimplePageDto;
import com.sinloingok.app.dtos.order.NetSiteOrderResDto;
import com.sinloingok.app.dtos.order.NetSiteUserReqDto;
import com.sinloingok.app.models.NetSiteCache;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.models.net4g.NetSiteOrder;
import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.service.user.UserMoneyRecordService;
import com.sinloingok.app.util.DateUtils;
import com.sinloingok.app.util.NextCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
public class NetSiteOrderService {

    @Autowired
    private NetSiteService netSiteService;
    @Autowired
    private NetSiteOrderDao netSiteOrderDao;

    @Autowired
    private NetSiteOrderItemizedService orderItemizedService;
    @Autowired
    private OrderSettlementService settlementService;

    public SimplePageDto<NetSiteOrderResDto> queryNetSiteOrderByPage(PageData<NetSiteUserReqDto> pageData) {
        NetSiteUserReqDto bean = pageData.getBody();
        long totalRecord = netSiteOrderDao.countNetSiteOrderByPage(bean);
        List<NetSiteOrderResDto> datas = netSiteOrderDao.queryNetSiteOrderByPage(bean, pageData);
        return new SimplePageDto<>(pageData, totalRecord, datas);
    }

    /**
     * 獲取用戶當前正在使用的訂單
     * @param userId
     * @param status
     * @return
     */
    public NetSiteOrder getCurrentOrder(Long userId, String status) {
        NetSiteOrder order = new NetSiteOrder();
        order.setUserId(userId);
        order.setStatus(status);
        return netSiteOrderDao.getDistinctNetSiteOrder(order);
    }

    /**
     * 獲取當前設備的訂單
     * @param onlyCode
     * @param status
     * @return
     */
    public NetSiteOrder getUsingOrder(String onlyCode, String status) {
        NetSiteOrder order = new NetSiteOrder();
        order.setOnlyCode(onlyCode);
        order.setStatus(status);
        return netSiteOrderDao.getDistinctNetSiteOrder(order);
    }

    /**
     * 獲取正在使用的訂單
     * @param status
     * @return
     */
    public List<NetSiteOrder> listByTypeAndStatus(String status) {
        return netSiteOrderDao.selectByTypeAndStatus(status);
    }

    /**
     * 根據訂單編號查詢訂單
     * @param payCode
     * @return
     */
    public NetSiteOrder selectByPayCode(String payCode) {
        return netSiteOrderDao.selectByPayCode(payCode);
    }

    /**
     * 查詢訂單狀態是否存在
     * @param payCode
     * @param status
     * @return
     */
    public boolean hasNetSiteOrderStatusExists(String payCode, String status) {
        return netSiteOrderDao.hasNetSiteOrderStatusExists(payCode, status);
    }

    public void createOrder(NetSite netSite, long userId, long voucherId) {
        NetSiteOrder order = new NetSiteOrder(netSite, userId, voucherId);
        netSiteOrderDao.insert(order);
        orderItemizedService.batchInsert(order, netSite.getAddressId());
        netSite.setStatus(NetSiteStatus.USING);
        netSiteService.update(netSite);
    }


    /**
     * 取消订单并结算
     *
     * @param payCode 订单编号
     * @throws Exception 处理过程中产生的任何异常
     */
    public NetSiteOrder cancelOrder(String payCode) {
        String message = "代理端结算";
        NetSiteOrder order = netSiteOrderDao.selectByPayCode(payCode);
        // 计算费用并自动扣除余额
        settlementService.calculateRemainingBalance(order);
        order.setCalc(message);
        settlementService.settleOrder(order);
        UserMoneyRecordService recordService = SBeanUtils.getBean(UserMoneyRecordService.class);
        recordService.createUserMoneyNetSiteOrderRecord(order, message);
        NetSite netSite = NetSiteCache.wscByOnlyCode(order.getOnlyCode());
        netSite.setStatus(NetSiteStatus.ONLINE);
        netSiteService.update(netSite);
        return order;
    }

    /**
     * 自動查詢沒有結算的訂單（超過20分鐘）
     * @return
     */
    public List<NetSiteOrder> autoOrderCheckNoRemainingBalance() {
        List<NetSiteOrder> list = listByTypeAndStatus(OrderStatus.USEING);
        Iterator<NetSiteOrder> iterator = list.iterator();
        while (iterator.hasNext()) {
            NetSiteOrder order = iterator.next();
            long seconds = DateUtils.secondsDifference(DateUtils.curTime(), order.getLastOperationTime());
            if (seconds <= 20 * 60) {
                iterator.remove();
            }
        }
        return list;
    }

    /**
     * 自动计算订单消费 是否超过用户余额
     *
     */
    public List<NetSiteOrder> autoCalculateRemainingBalance() {
        List<NetSiteOrder> list = listByTypeAndStatus(OrderStatus.USEING);
        Iterator<NetSiteOrder> iterator = list.iterator();
        while (iterator.hasNext()) {
            NetSiteOrder order = iterator.next();
            if (!settlementService.calculateRemainingBalance(order)) {
                log.info("脈衝結束後餘額檢查: total_money={} 餘額不足，自動結算", order.getNetAmount());
                settlementService.settleOrder(order);
                log.warn("設備 {} 餘額不足，自動結算 orderId={}", order.getOnlyCode(), order.getId());
            }else{
                iterator.remove();
            }
        }
        return list;
    }
}
