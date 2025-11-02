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

    public static class SettlementResult {
        private final boolean executed;
        private final boolean success;
        private final String detail;
        private final long costMs;

        private SettlementResult(boolean executed, boolean success, String detail, long costMs) {
            this.executed = executed;
            this.success = success;
            this.detail = detail;
            this.costMs = costMs;
        }

        public static SettlementResult skipped(String detail) {
            return new SettlementResult(false, true, detail, 0L);
        }

        public static SettlementResult success(String detail, long costMs) {
            return new SettlementResult(true, true, detail, costMs);
        }

        public static SettlementResult failed(String detail, long costMs) {
            return new SettlementResult(true, false, detail, costMs);
        }

        public boolean isExecuted() {
            return executed;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getDetail() {
            return detail;
        }

        public long getCostMs() {
            return costMs;
        }
    }
    @Autowired
    private NetSiteOrderService netSiteOrderService;
    @Autowired
    private NetSiteOrderItemizedService orderItemizedService;

    public SettlementResult settleChannel(String onlyCode, int channel, DeviceControl.Action command) {
        NetSiteOrder order = netSiteOrderService.getUsingOrder(onlyCode, OrderStatus.USEING);
        if (order == null) {
            log.debug("trace={} phase=settle step=skip msg=no_using_order", MDC.get("trace"));
            return SettlementResult.skipped("no_using_order");
        }
        NetSiteOrderItemized itemized = order.getOrderItemized(SignalTopology.getFunctionCode(channel));
        if (itemized == null) {
            log.debug("trace={} phase=settle step=skip msg=no_itemized ch={}", MDC.get("trace"), channel);
            return SettlementResult.skipped("no_itemized");
        }
        long t0 = System.nanoTime();
        log.debug("trace={} phase=settle step=calc_start ch={} funcCode={} funcName={} cmd={}",
                MDC.get("trace"),
                channel, SignalTopology.getFunctionCode(channel), SignalTopology.getFunctionName(channel), command.name());

        if (DeviceControl.Action.OPEN.equals(command)) {
            itemized.runCalculate();
        } else if (DeviceControl.Action.CLOSE.equals(command)) {
            itemized.ruleFinishCalculate();
        }
        orderItemizedService.updateSelective(itemized);

        long costMs = (System.nanoTime() - t0) / 1_000_000;
        log.debug("trace={} phase=settle step=calc_done ch={} costMs={}", MDC.get("trace"), channel, costMs);
        String detail = String.format("channel=%d func=%s", channel, SignalTopology.getFunctionCode(channel));
        return SettlementResult.success(detail, costMs);
    }

    public SettlementResult settleAll(String onlyCode){
        long t0 = System.nanoTime();
        NetSiteOrder order = netSiteOrderService.getUsingOrder(onlyCode, OrderStatus.USEING);
        if (order != null) {
            netSiteOrderService.cancelOrder(order.getPayCode());
            long costMs = (System.nanoTime() - t0) / 1_000_000;
            log.debug("trace={} phase=settle step=all_done costMs={}", MDC.get("trace"), costMs);
            String detail = String.format("all payCode=%s", order.getPayCode());
            return SettlementResult.success(detail, costMs);
        }
        log.debug("trace={} phase=settle step=skip msg=no_using_order", MDC.get("trace"));
        return SettlementResult.skipped("no_using_order");
    }
}
