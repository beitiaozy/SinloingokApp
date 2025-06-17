package com.sinloingok.app.scheduled;

import com.sinloingok.app.config.SBeanUtils;
import com.sinloingok.app.service.order.OrderFulfillmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 自动提醒和结算Job
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Slf4j
@Component
public class OrderRemindScheduled {

    @Scheduled(fixedDelay = 60000)
    public void execute() {
        try {
            remind();
        } catch (Exception e) {
            log.error("OrderRemindJob error", e);
        }
    }


    /**
     * 15分钟无清水脉冲信号提醒
     * 20分钟以上需要自动结算
     */
    public void remind() {
        OrderFulfillmentService orderFulfillmentService = SBeanUtils.getBean(OrderFulfillmentService.class);
        orderFulfillmentService.autoCompleteOrderAndReleaseDevice();
    }
}
