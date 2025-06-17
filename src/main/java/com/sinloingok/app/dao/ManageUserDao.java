package com.sinloingok.app.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sinloingok.app.models.user.ManageUser;

/**
 * DAO for ManageUser queries
 */
@Mapper
public interface ManageUserDao {

    ManageUser selectById(@Param("id") Long id);

    List<String> selectStatusesByApplyUserId(@Param("applyUserId") Long applyUserId);

    /**
     * 插入用户
     * @param manageUser 用户对象
     * @return 影响行数
     */
    int insert(ManageUser manageUser);

    /**
     * 更新用户
     * @param manageUser 用户对象
     * @return 影响行数
     */
    int update(ManageUser manageUser);


    /**
     * 根据条件查询用户列表
     * @param manageUser 查询条件
     * @return 用户列表
     */
    List<ManageUser> queryByManageUser(ManageUser manageUser);
}
