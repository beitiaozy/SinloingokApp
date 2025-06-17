package com.sinloingok.app.models.activity;

import lombok.Data;

/**
 * 平台公告、通知
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
public class PlatformNotice {

    /**
     *
     */
    private static final long serialVersionUID = 1440323830312920345L;


    private Long id;
    private String status;
    private String title;
    private String content;
    private String flag;
    private String description;
    private String beginTime;
    private String endTime;
    private String createTime;
    private String updateTime;

}
