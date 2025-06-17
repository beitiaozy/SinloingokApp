package com.sinloingok.app.scheduled;

import com.sinloingok.app.config.SBeanUtils;
import com.sinloingok.app.models.NetSiteCache;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.service.order.OrderFulfillmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 每分钟定时任务列表
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Slf4j
@Component
public class CyclePerMinScheduled {

    @Scheduled(fixedDelay = 1000)
    public void execute() {
        checkNetSiteOnlineScheduled();
    }

    /**
     * 检查设备是否在线
     */
    private void checkNetSiteOnlineScheduled(){
        NetSiteService netSiteService = SBeanUtils.getBean(NetSiteService.class);
        List<NetSite> list = netSiteService.checkNetSiteOnline();
        NetSiteCache.refresh(list);
        OrderFulfillmentService fulfillmentService = SBeanUtils.getBean(OrderFulfillmentService.class);
        fulfillmentService.autoCheckUnUsingDevice();
    }

}
