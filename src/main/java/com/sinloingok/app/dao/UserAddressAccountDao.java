package com.sinloingok.app.dao;

import com.sinloingok.app.models.fund.UserAddressAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserAddressAccountDao {
    UserAddressAccount findByUserAndAddress(@Param("userId") Long userId,
                                            @Param("addressId") Long addressId);

    int insertOrUpdateUserAccount(UserAddressAccount account);

    int updateBalance(@Param("id") Long id,
                      @Param("balance") BigDecimal balance,
                      @Param("extMoney") BigDecimal extMoney);

    /**
     * 查询用户在多个场地的账户
     */
    List<UserAddressAccount> findByUserAndAddresses(@Param("userId") Long userId,
                                                    @Param("addressIds") List<Long> addressIds);

    /**
     * 汇总用户在所有场地的充值余额
     */
    @Select("SELECT COALESCE(SUM(balance), 0) FROM user_address_account WHERE user_id = #{userId}")
    BigDecimal sumBalanceByUser(@Param("userId") Long userId);

    /**
     * 汇总用户在所有场地的赠送金额
     */
    @Select("SELECT COALESCE(SUM(ext_money), 0) FROM user_address_account WHERE user_id = #{userId}")
    BigDecimal sumExtMoneyByUser(@Param("userId") Long userId);
}