package com.sinloingok.app.dao;

import java.util.List;
import java.util.Map;

import com.sinloingok.app.models.voucher.UserVoucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserVoucherDao {
    List<UserVoucher> selectExpired(@Param("status") String status,
                                   @Param("curTime") String curTime);

    List<Map<String, Object>> selectByUser(@Param("userId") Long userId,
                                           @Param("status") String status,
                                           @Param("type") String type,
                                           @Param("addressId") Long addressId);

    UserVoucher selectById(@Param("id") Long id);

    int insert(UserVoucher voucher);

    int update(UserVoucher voucher);
}
