package com.sinloingok.app.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.sinloingok.app.models.user.Feedback;

@Mapper
public interface FeedbackDao {
    @Select("SELECT id, user_id AS userId, title, mobile, content, create_time AS createTime FROM feedback WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Feedback> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id AS userId, title, mobile, content, create_time AS createTime FROM feedback WHERE id = #{id} LIMIT 1")
    Feedback selectById(@Param("id") Long id);

    @org.apache.ibatis.annotations.Delete("DELETE FROM feedback WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @org.apache.ibatis.annotations.Insert("INSERT INTO feedback(user_id,title,mobile,content,create_time) VALUES(#{userId},#{title},#{mobile},#{content},#{createTime})")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Feedback feedback);
}
