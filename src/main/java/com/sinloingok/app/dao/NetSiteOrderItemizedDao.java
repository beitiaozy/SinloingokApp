package com.sinloingok.app.dao;

import com.sinloingok.app.models.net4g.NetSiteOrderItemized;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface NetSiteOrderItemizedDao {
    
    /**
     * 插入单条记录
     */
    int insert(NetSiteOrderItemized record);
    
    /**
     * 根据字段为空动态更新
     */
    int updateSelective(NetSiteOrderItemized record);
    
    /**
     * 根据条件查询列表
     */
    List<NetSiteOrderItemized> selectByCondition(Map<String, Object> condition);
    

    /**
     * 根据唯一键查询
     */
    NetSiteOrderItemized selectByUniqueKey(@Param("payCode") String payCode, @Param("onlyCodePort") Integer onlyCodePort);
}