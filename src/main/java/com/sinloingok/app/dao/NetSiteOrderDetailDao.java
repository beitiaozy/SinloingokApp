package com.sinloingok.app.dao;

import com.sinloingok.app.models.net4g.NetSiteOrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NetSiteOrderDetailDao {
    NetSiteOrderDetail selectLatest(@Param("orderId") Long orderId,
                                    @Param("type") Integer type);
    NetSiteOrderDetail selectOpenDetail(@Param("orderId") Long orderId,
                                        @Param("type") Integer type);

    java.util.List<NetSiteOrderDetail> selectByOrderId(@Param("orderId") Long orderId);

    int insert(@Param("bean") NetSiteOrderDetail detail);


    int update(@Param("bean") NetSiteOrderDetail detail);
}
