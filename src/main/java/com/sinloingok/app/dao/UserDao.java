package com.sinloingok.app.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.sinloingok.app.models.user.User;

@Mapper
public interface UserDao {
    User selectById(@Param("id") Long id, @Param("addressId") long addressId);
    // 分页查询用户列表
    List<User> selectByMobileLike(
            @Param("mobile") String mobile,
            @Param("addressId") long addressId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    // 查询符合条件的总记录数
    Long countByMobileLike(@Param("mobile") String mobile, @Param("addressId") long addressId);
    User selectByOpenId(@Param("openId") String openId, @Param("addressId") long addressId);

    int updateMoney(@Param("userId") long userId, @Param("balance") float balance, @Param("extMoney") float extMoney);

    int insertOrUpdateUser(User user);

    int update(User user);
}
