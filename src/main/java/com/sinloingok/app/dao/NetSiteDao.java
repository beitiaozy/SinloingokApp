package com.sinloingok.app.dao;

import java.util.List;
import java.util.Map;

import com.sinloingok.app.controllers.base.PageData;
import com.sinloingok.app.dtos.NetSiteStatsResDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.sinloingok.app.models.net4g.NetSite;

@Mapper
public interface NetSiteDao {


    List<NetSite> selectNetSitePage(@Param("bean")NetSite bean, @Param("page") PageData page);
    int countNetSites(@Param("bean")NetSite bena);

    NetSite selectByOnlyCode(@Param("onlyCode") String onlyCode);
    List<NetSite> selectActive();
    NetSite selectById(@Param("id") Long id);

    int updateTerminal(@Param("id") Long id,
                       @Param("terminalSn") String terminalSn,
                       @Param("terminalKey") String terminalKey);

    int update(NetSite netSite);

    int insertOrUpdateNetSite(NetSite netSite);

    NetSiteStatsResDto selectNetSiteStats();
}
