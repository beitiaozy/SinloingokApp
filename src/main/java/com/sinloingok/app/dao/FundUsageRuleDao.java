package com.sinloingok.app.dao;

import com.sinloingok.app.models.fund.FundUsageRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
@Mapper
public interface FundUsageRuleDao {
    int saveOrUpdate(FundUsageRule rule);
    
    List<Long> findSourceAddressesForTarget(@Param("targetAddressId") Long targetAddressId);

    /**
     * 检查资金使用规则是否存在并启用
     */
    @Select("SELECT COUNT(*) > 0 FROM fund_usage_rule " +
            "WHERE source_address_id = #{sourceAddressId} " +
            "AND target_address_id = #{targetAddressId} " +
            "AND status = 1")
    boolean checkUsageRule(@Param("sourceAddressId") Long sourceAddressId,
                           @Param("targetAddressId") Long targetAddressId);
}