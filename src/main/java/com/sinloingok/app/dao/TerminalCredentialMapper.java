package com.sinloingok.app.dao;

import com.sinloingok.app.models.payment.TerminalCredential;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// com.sinloingok.app.mapper.TerminalCredentialMapper
@Mapper
public interface TerminalCredentialMapper {

    TerminalCredential selectByOnlyCode(@Param("onlyCode") String onlyCode);

    TerminalCredential selectBySiteAndAccount(@Param("netSiteId") Long netSiteId, @Param("accountId") Long accountId);

    List<TerminalCredential> listByAccount(@Param("accountId") Long accountId);

    int insert(TerminalCredential record);

    int updateById(TerminalCredential record);
}
