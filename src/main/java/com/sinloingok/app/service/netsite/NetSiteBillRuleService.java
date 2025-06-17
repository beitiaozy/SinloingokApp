package com.sinloingok.app.service.netsite;

import com.sinloingok.app.dao.NetSiteBillRuleDao;
import com.sinloingok.app.models.net4g.NetSiteBillRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NetSiteBillRuleService {

    @Autowired
    private NetSiteBillRuleDao billRuleDao;

    public List<NetSiteBillRule> listNetSiteBillRule(long addressId){
        return billRuleDao.selectRuleByAddress(addressId);
    }
}
