package com.sinloingok.app.service.order;

import com.sinloingok.app.dao.NetSiteOrderItemizedDao;
import com.sinloingok.app.models.net4g.NetSiteBillRule;
import com.sinloingok.app.models.net4g.NetSiteOrder;
import com.sinloingok.app.models.net4g.NetSiteOrderItemized;
import com.sinloingok.app.service.netsite.NetSiteBillRuleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class NetSiteOrderItemizedService {
    
    @Autowired
    private NetSiteOrderItemizedDao orderItemizedDao;

    @Autowired
    private NetSiteBillRuleService ruleService;

    /**
     * 根據訂單批量初始化收費明細
     */
    public boolean batchInsert(NetSiteOrder order, long addressId) {
        if (order == null || StringUtils.isEmpty(order.getPayCode())) {
            return false;
        }
        List<NetSiteBillRule> rules = ruleService.listNetSiteBillRule(addressId);
        for (NetSiteBillRule rule : rules) {
            NetSiteOrderItemized itemized = new NetSiteOrderItemized(rule, order.getPayCode(), order.getOnlyCode());
            orderItemizedDao.insert(itemized);
        }
        return true;
    }
    
    /**
     * 动态更新
     */
    public boolean updateSelective(NetSiteOrderItemized record) {
        return orderItemizedDao.updateSelective(record) > 0;
    }
    
    /**
     * 根据条件查询列表
     */
    public List<NetSiteOrderItemized> selectByCondition(String ruleCode, String payCode,
                                                        String onlyCode, Integer onlyCodePort,
                                                        Date startTime, Date endTime,
                                                        BigDecimal minAmount, BigDecimal maxAmount,
                                                        Integer limit) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("ruleCode", ruleCode);
        condition.put("payCode", payCode);
        condition.put("onlyCode", onlyCode);
        condition.put("onlyCodePort", onlyCodePort);
        condition.put("startTime", startTime);
        condition.put("endTime", endTime);
        condition.put("minAmount", minAmount);
        condition.put("maxAmount", maxAmount);
        condition.put("limit", limit);
        
        return orderItemizedDao.selectByCondition(condition);
    }
    
    /**
     * 根据订单号查询所有消费记录
     */
    public List<NetSiteOrderItemized> selectByPayCode(String payCode) {
        return selectByCondition(null, payCode, null, null, null, null, null, null, null);
    }

    /**
     * 根据订单号和端口號查询單個
     */
    public NetSiteOrderItemized selectByUniqueKey(String payCode, int onlyCodePort){
        return orderItemizedDao.selectByUniqueKey(payCode, onlyCodePort);
    }
}