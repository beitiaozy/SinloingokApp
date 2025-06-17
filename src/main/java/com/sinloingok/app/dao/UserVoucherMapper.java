package com.sinloingok.app.dao;

import com.sinloingok.app.models.voucher.UserVoucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserVoucherMapper {

    /**
     * 根据条件查询卡券列表
     * @param userVoucher 查询条件
     * @return 卡券列表
     */
    List<UserVoucher> selectByVoucher(UserVoucher userVoucher);

    /**
     * 根据ID查询卡券
     * @param id 卡券ID
     * @return 卡券信息
     */
    UserVoucher selectVoucherById(Long id);

    /**
     * 新增卡券
     * @param userVoucher 卡券信息
     * @return 影响行数
     */
    int insert(UserVoucher userVoucher);

    /**
     * 批量新增卡券
     * @param userVoucherList 卡券列表
     * @return 影响行数
     */
    int insertList(List<UserVoucher> userVoucherList);

    /**
     * 更新卡券信息
     * @param userVoucher 卡券信息
     * @return 影响行数
     */
    int update(UserVoucher userVoucher);

    /**
     * 根据ID删除卡券
     * @param id 卡券ID
     * @return 影响行数
     */
    int deleteById(Long id);
}