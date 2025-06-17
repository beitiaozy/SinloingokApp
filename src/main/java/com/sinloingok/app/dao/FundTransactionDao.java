package com.sinloingok.app.dao;

import com.sinloingok.app.models.fund.FundTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FundTransactionDao {
    /**
     * 插入交易记录
     */
    int insert(FundTransaction transaction);
    
    /**
     * 根据交易订单号查询
     */
    FundTransaction findByPayCode(@Param("payCode") String payCode);
    
    /**
     * 更新交易状态和消息
     */
    int updateStatusAndMessage(@Param("id") Long id, 
                             @Param("status") Integer status,
                             @Param("message") String message);
}