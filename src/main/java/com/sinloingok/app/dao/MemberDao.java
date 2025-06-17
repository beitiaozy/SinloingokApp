package com.sinloingok.app.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sinloingok.app.models.user.User;
import com.sinloingok.app.models.user.Feedback;

@Mapper
public interface MemberDao {
    List<User> selectMemberList(@Param("memberName") String memberName,
                               @Param("mobile") String mobile,
                               @Param("userStatus") String userStatus,
                               @Param("commissionStatus") String commissionStatus,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    Long countMemberList(@Param("memberName") String memberName,
                         @Param("mobile") String mobile,
                         @Param("userStatus") String userStatus,
                         @Param("commissionStatus") String commissionStatus);

    List<Feedback> selectFeedbackList(@Param("searchNickname") String searchNickname,
                                      @Param("searchMobile") String searchMobile,
                                      @Param("searchContent") String searchContent,
                                      @Param("begin") String begin,
                                      @Param("end") String end,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);

    Long countFeedbackList(@Param("searchNickname") String searchNickname,
                           @Param("searchMobile") String searchMobile,
                           @Param("searchContent") String searchContent,
                           @Param("begin") String begin,
                           @Param("end") String end);
}
