package com.sinloingok.app.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.sinloingok.app.models.activity.ActivityIntro;

@Mapper
public interface ActivityIntroDao {
    @Select("SELECT id, type, title, description, bank FROM activity_intro ORDER BY id DESC LIMIT 1")
    ActivityIntro selectLatest();

    @Select("SELECT id, type, title, description, bank FROM activity_intro WHERE id = #{id} LIMIT 1")
    ActivityIntro selectById(Long id);
}
