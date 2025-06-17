package com.sinloingok.app.service.order;

import com.sinloingok.app.constant.SignalTopology;
import com.sinloingok.app.dao.status.NetSiteStatus;
import com.sinloingok.app.dao.status.OrderStatus;
import com.sinloingok.app.models.DeviceControl;
import com.sinloingok.app.models.NetSiteCache;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.models.net4g.NetSiteOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OrderFulfillmentService {

    /**
     * 记录上一次空压机状态，避免重复触发
     * true  = 已关闭
     * false = 已开启（或正在使用中）
     */
    private volatile boolean airCompressorOff = false;
    @Autowired
    private NetSiteOrderService netSiteOrderService;

    @Autowired
    private CommandExecutor executor;


    // 1. 创建订单并开启设备
    public void createOrderAndActivateDevice(NetSite netSite, long userId, long voucherId) {
        netSiteOrderService.createOrder(netSite, userId, voucherId);
        executor.execute(netSite.getOnlyCode(), SignalTopology.getChannelNumber(SignalTopology.CH_QS1_8), DeviceControl.Action.OPEN);
        executor.turnOnPmNetSiteDevice(netSite.getOnlyCode());
        executor.turnOnAirCompressorPower();
    }

    // 3. 正常结束订单并关闭设备
    public void completeOrderAndReleaseDevice(String payCode) {
        NetSiteOrder order = netSiteOrderService.selectByPayCode(payCode);
        executor.execute(order.getOnlyCode(), SignalTopology.getChannelNumber(SignalTopology.CH_GJ_6), DeviceControl.Action.CLOSE);
        executor.turnOffPmNetSiteDevice(order.getOnlyCode());
    }

    /**
     * 1/自動結算查過20分鐘，認為結算的用戶
     * 2、用戶餘額不足自動結算
     *
     */
    public void autoCompleteOrderAndReleaseDevice() {
        completeOrderAndReleaseDeviceList(netSiteOrderService.autoOrderCheckNoRemainingBalance());
//        completeOrderAndReleaseDeviceList(netSiteOrderService.autoCalculateRemainingBalance());
    }

    private void completeOrderAndReleaseDeviceList(List<NetSiteOrder> list){
        for (NetSiteOrder order : list) {
            completeOrderAndReleaseDevice(order.getPayCode());
        }
    }

    /**
     * 自动校验设备数据
     * 1. 若无使用中订单，仅首次检测到空闲时关闭空压机；
     * 2. 若有使用中订单，且上次为空闲状态，则恢复标记（可重新触发关闭）
     */
    public void autoCheckUnUsingDevice() {
//        List<NetSite> netSites = NetSiteCache.wscSnapshotNetSite();
//        for (int i = 0; i < netSites.size(); i++) {
//            NetSite ns = netSites.get(i);
//            if(ns.getStatus().equals(NetSiteStatus.USING)){
//                if(netSiteOrderService.getUsingOrder(OrderStatus.USEING, ns.getOnlyCode()) == null){
//                    ns.setStatus(NetSiteStatus.ONLINE);
//                    NetSiteCache.upsert(ns);
//                }
//            }
//        }

        List<NetSiteOrder> list = netSiteOrderService.listByTypeAndStatus(OrderStatus.USEING);
        // 情况 1: 没有正在使用的订单
        if (CollectionUtils.isEmpty(list)) {
            // 如果已经关闭过，则不再重复执行
            if (!airCompressorOff) {
                log.info("[autoCheck] 未检测到使用中订单 -> 关闭空压机");
                executor.turnOffAirCompressPower();
                airCompressorOff = true; // 标记为已关闭
            }
            return;
        }
        // 情况 2: 有使用中订单  状态回退（说明重新有人使用设备）
        if (airCompressorOff) {
            log.info("[autoCheck] 检测到使用中订单 -> 标记空压机为运行中");
            airCompressorOff = false;
        }

        // ✅ 这里写 刷新设备状态、补水脉冲
        // executor.execute(...);
    }


}
