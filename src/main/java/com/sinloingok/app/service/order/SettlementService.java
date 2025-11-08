package com.sinloingok.app.service.order;

import com.sinloingok.app.constant.SignalTopology;
import com.sinloingok.app.dao.status.OrderStatus;
import com.sinloingok.app.models.DeviceControl;
import com.sinloingok.app.models.net4g.NetSiteOrder;
import com.sinloingok.app.models.net4g.NetSiteOrderItemized;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SettlementService {
    @Autowired
    private NetSiteOrderService netSiteOrderService;
    @Autowired
    private NetSiteOrderItemizedService orderItemizedService;

    public void settleChannel(String onlyCode, int channel, DeviceControl.Action command) {
        NetSiteOrder order = netSiteOrderService.getUsingOrder(onlyCode, OrderStatus.USEING);
        if (order == null) {
            log.info("trace={} phase=settle step=skip msg=no_using_order", MDC.get("trace"));
            return;
        }
        NetSiteOrderItemized itemized = order.getOrderItemized(SignalTopology.getFunctionCode(channel));
        if (itemized == null) {
            log.info("trace={} phase=settle step=skip msg=no_itemized ch={}", MDC.get("trace"), channel);
            return;
        }
        long t0 = System.nanoTime();
        log.info("trace={} phase=settle step=calc_start ch={} funcCode={} funcName={} cmd={}",
                MDC.get("trace"),
                channel, SignalTopology.getFunctionCode(channel), SignalTopology.getFunctionName(channel), command.name());

        if (DeviceControl.Action.OPEN.equals(command)) {
            itemized.runCalculate();
        } else if (DeviceControl.Action.CLOSE.equals(command)) {
            itemized.ruleFinishCalculate();
        }
        orderItemizedService.updateSelective(itemized);

        long costMs = (System.nanoTime() - t0) / 1_000_000;
        log.info("trace={} phase=settle step=calc_done ch={} costMs={}", MDC.get("trace"), channel, costMs);
    }

    public void settleAll(String onlyCode){
        long t0 = System.nanoTime();
        NetSiteOrder order = netSiteOrderService.getUsingOrder(onlyCode, OrderStatus.USEING);
        if (order != null) {
            netSiteOrderService.cancelOrder(order.getPayCode());
        }
        long costMs = (System.nanoTime() - t0) / 1_000_000;
        log.info("trace={} phase=settle step=all_done costMs={}", MDC.get("trace"), costMs);
    }
}
