package com.sinloingok.app.dao;

import com.sinloingok.app.models.payment.PaymentAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// com.sinloingok.app.mapper.PaymentAccountMapper
@Mapper
public interface PaymentAccountMapper {
    PaymentAccount selectById(@Param("id") Long id);

    List<PaymentAccount> listEnabled();

    int insert(PaymentAccount record);

    int updateById(PaymentAccount record);
}
