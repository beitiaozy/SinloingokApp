package com.sinloingok.app.dao;

import com.sinloingok.app.dtos.user.RechargeRecordDto;
import com.sinloingok.app.dtos.user.UserMoneyRecordStatDto;
import com.sinloingok.app.models.user.UserMoneyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * DAO for UserMoneyRecord queries
 */
@Mapper
public interface UserMoneyRecordDao {

    List<RechargeRecordDto> selectRechargeRecord(@Param("types") List<Integer> types, @Param("userId") long userId,
                                                 @Param("offset") int offset,
                                                 @Param("limit") int limit);

    long countRechargeRecord(@Param("types") List<Integer> types, @Param("userId") long userId);

    UserMoneyRecord selectByOrderIdGte(@Param("orderId") long orderId);

    int insertRechargeRecord(UserMoneyRecord record);

    List<UserMoneyRecordStatDto> selectGroupedMoneyStats();
}
