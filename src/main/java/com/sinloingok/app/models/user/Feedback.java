package com.sinloingok.app.models.user;

import lombok.Data;

/**
 * 用户反馈
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
public class Feedback {

    /**
     *
     */
    private static final long serialVersionUID = 5863313269123567632L;


    private Long id;
    private Long shopId;
    private String content;
    private Long userId;
    private String createTime;
    private String title;
    private String mobile;

    private String nickname;

}
