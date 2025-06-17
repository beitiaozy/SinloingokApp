package com.sinloingok.app.service.netsite;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.sinloingok.app.config.SBeanUtils;
import com.sinloingok.app.controllers.base.PageData;
import com.sinloingok.app.dao.status.OrderStatus;
import com.sinloingok.app.dtos.NetSiteStatsResDto;
import com.sinloingok.app.service.order.NetSiteOrderService;
import com.sinloingok.app.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinloingok.app.dao.NetSiteDao;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.dao.status.NetSiteStatus;
import com.sinloingok.app.dtos.SimplePageDto;

/**
 * Service layer for NetSite operations
 */
@Service
public class NetSiteService {

    @Autowired
    private NetSiteDao netSiteDao;

    /**
     * Retrieve a NetSite by its unique code.
     */
    public NetSite selectByOnlyCode(String onlyCode) {
        return netSiteDao.selectByOnlyCode(onlyCode);
    }


    /**
     * List all active NetSites.
     */
    public List<NetSite> listActive() {
        return netSiteDao.selectActive();
    }

    /**
     * Retrieve a NetSite by ID.
     */
    public NetSite selectById(Long id) {
        return netSiteDao.selectById(id);
    }

    /**
     * Retrieve paged VM device list.
     */
    public SimplePageDto<NetSite> vmDeviceList(NetSite bean, PageData pageData) {
        List<NetSite> datas = netSiteDao.selectNetSitePage(bean, pageData);
        int totalRecord = netSiteDao.countNetSites(bean);
        return new SimplePageDto<>(pageData.getPageNo(), pageData.getPageSize(), totalRecord, datas);
    }

    public void updateTerminal(Long id, String terminalSn, String terminalKey) {
        netSiteDao.updateTerminal(id, terminalSn, terminalKey);
    }

    public void update(NetSite netSite) {
        netSiteDao.update(netSite);
    }

    public void insertOrUpdateNetSite(NetSite netSite) {
        netSiteDao.insertOrUpdateNetSite(netSite);
    }

    /**
     * 自动注册这部分逻辑展缓
     * @param only_code
     */
    public void registerOrRefreshNetSite(String only_code){
        NetSite netSite = selectByOnlyCode(only_code);
        if (netSite != null) {
            if (netSite.getStatus().equals(NetSiteStatus.OFFLINE)) {
                // 彌補設備在使用中掉線後重連
                NetSiteOrderService orderService = SBeanUtils.getBean(NetSiteOrderService.class);
                if(orderService.getUsingOrder(only_code, OrderStatus.USEING) != null){
                    netSite.setStatus(NetSiteStatus.USING);
                }else{
                    netSite.setStatus(NetSiteStatus.ONLINE);
                }
            }
            netSite.refresh(DateUtils.curTime());
            update(netSite);
        }
    }

    public NetSiteStatsResDto selectNetSiteStats(){
        return netSiteDao.selectNetSiteStats();
    }

    /**
     * 检查所有终端设备收否在线
     */
    public List<NetSite> checkNetSiteOnline(){
        List<NetSite> list = listActive();
        list.stream()
                .filter(netSite -> DateUtils.secondsDifference(DateUtils.curTime(), netSite.getHeartTime()) > 120)
                .forEach(netSite -> {
                    netSite.setStatus(NetSiteStatus.OFFLINE);
                    netSite.setUpdateTime(DateUtils.curTime());
                    netSiteDao.update(netSite);
                });
        return list;
    }
}
