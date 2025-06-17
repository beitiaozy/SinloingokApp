package com.sinloingok.app.dao;

import java.util.List;
import java.util.Map;

import com.sinloingok.app.controllers.base.PageData;
import com.sinloingok.app.dtos.order.NetSiteOrderResDto;
import com.sinloingok.app.dtos.order.NetSiteUserReqDto;
import com.sinloingok.app.models.net4g.NetSiteOrder;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NetSiteOrderDao {

    List<NetSiteOrderResDto> queryNetSiteOrderByPage(@Param("bean")NetSiteUserReqDto bean, @Param("page") PageData page);

    int countNetSiteOrderByPage(@Param("bean")NetSiteUserReqDto bean);

    List<NetSiteOrder> selectByTypeAndStatus(@Param("status") String status);

    NetSiteOrder selectByPayCode(@Param("payCode") String payCode);

    boolean hasNetSiteOrderStatusExists(@Param("payCode") String payCode, @Param("status") String status);

    int insert(NetSiteOrder order);

    int update(NetSiteOrder order);

    NetSiteOrder getDistinctNetSiteOrder(NetSiteOrder order);
}
