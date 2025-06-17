package com.sinloingok.app.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.sinloingok.app.models.activity.PlatformNotice;

@Mapper
public interface PlatformNoticeDao {
    @Select("SELECT id, content, title, create_time AS createTime FROM platform_notice WHERE status != '已删除' ORDER BY create_time DESC")
    List<PlatformNotice> selectActive();
}
