package com.sinloingok.app.dao;

import com.sinloingok.app.models.net4g.NetSiteBillRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NetSiteBillRuleDao {
    
    /**
     * 根据规则编号查询计费规则
     * @param ruleCode 规则编号
     * @return 计费规则对象
     */
    NetSiteBillRule selectRuleByCode(@Param("ruleCode") String ruleCode);

    List<NetSiteBillRule> selectRuleByAddress(@Param("addressId") long addressId);
    /**
     * 插入或更新计费规则
     * 如果ruleCode已存在则更新，否则插入
     * @param rule 计费规则对象
     * @return 影响的行数
     */
    int insertOrUpdateRule(NetSiteBillRule rule);
    
    /**
     * 检查规则编号是否存在
     * @param ruleCode 规则编号
     * @return 存在的记录数
     */
    int checkRuleCodeExists(@Param("ruleCode") String ruleCode);
}