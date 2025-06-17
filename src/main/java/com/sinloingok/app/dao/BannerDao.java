package com.sinloingok.app.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.sinloingok.app.dtos.common.NetSiteInfoDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sinloingok.app.models.home.HomeImg;

@Mapper
public interface BannerDao {
    List<HomeImg> selectByActivityType(@Param("activityType") String activityType);


    NetSiteInfoDto selectInfoByOnlyCode(@Param("onlyCode") String onlyCode,
                                        @Param("status") String status);

    List<Map<String, BigDecimal>> selectSlgFreeMapper();

}
