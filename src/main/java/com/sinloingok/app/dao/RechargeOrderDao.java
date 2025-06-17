package com.sinloingok.app.dao;

import com.sinloingok.app.controllers.base.PageData;
import com.sinloingok.app.dtos.user.RechargeOrderResDto;
import com.sinloingok.app.dtos.user.RechargeRecordDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sinloingok.app.models.user.RechargeOrder;

import java.util.List;
import java.util.Map;

/**
 * DAO for RechargeOrder queries
 */
@Mapper
public interface RechargeOrderDao {


    List<RechargeOrderResDto> selectRechargeOrderByPage(@Param("bean") RechargeRecordDto dto, @Param("page") PageData page);
    int countRechargeOrder(@Param("bean") RechargeRecordDto dto, @Param("page") PageData page);

    List<RechargeOrderResDto> selectRechargeOrderByCondition(@Param("bean") RechargeRecordDto dto);

    Map sumMoneyAwards(@Param("bean") RechargeRecordDto dto);

    RechargeOrder selectByCodeAndUser(@Param("code") String code,
                                      @Param("userId") Long userId);

    RechargeOrder selectLatestByUserAndStatus(@Param("userId") Long userId,
                                              @Param("status") String status);

    int insert(RechargeOrder order);

    RechargeOrder selectById(@Param("id") Long id);

    int update(RechargeOrder order);
}
