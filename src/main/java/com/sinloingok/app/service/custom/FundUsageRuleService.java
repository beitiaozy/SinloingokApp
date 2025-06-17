package com.sinloingok.app.service.custom;

import com.sinloingok.app.dao.FundUsageRuleDao;
import com.sinloingok.app.models.fund.FundUsageRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FundUsageRuleService {
    @Autowired
    private FundUsageRuleDao fundUsageRuleDao;
    
    /**
     * 设置资金使用规则
     * @param sourceAddressId 充值来源场地
     * @param targetAddressId 可使用资金的场地
     * @param enable 是否启用
     */
    public void setUsageRule(Long sourceAddressId, Long targetAddressId, boolean enable) {
        FundUsageRule rule = new FundUsageRule();
        rule.setSourceAddressId(sourceAddressId);
        rule.setTargetAddressId(targetAddressId);
        rule.setStatus(enable ? 1 : 0);
        fundUsageRuleDao.saveOrUpdate(rule);
    }
    
    /**
     * 检查是否允许使用资金
     */
    public boolean isUsageAllowed(Long sourceAddressId, Long targetAddressId) {
        return fundUsageRuleDao.checkUsageRule(sourceAddressId, targetAddressId);
    }

    public List<Long> findSourceAddressesForTarget(Long addressId) {
        return fundUsageRuleDao.findSourceAddressesForTarget(addressId);
    }
}