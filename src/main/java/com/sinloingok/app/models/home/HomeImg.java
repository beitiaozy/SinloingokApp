package com.sinloingok.app.models.home;

import lombok.Data;

/**
 * 首页轮播图片实体
 */
@Data
public class HomeImg {

    private static final long serialVersionUID = -6226713700786327955L;


    private Long id;
    private String img;
    private Long shopId;
    private Long cityId;
    private String activityType;
    private String districtCode;
    private String description;
    private String otherUrl;
    private Long goodsId;
    private String createTime;
    private String updateTime;
}
