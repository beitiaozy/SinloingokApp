package com.sinloingok.app.dao.status;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 所有业务枚举的通用接口
 */
public interface BaseEnum {
    /**
     * 数据库存储/序列化的唯一标识
     */
    @JsonValue
    String getCode();

    /**
     * 前端展示用的名称
     */
    String getGenreName();

    /**
     * 枚举的详细描述
     */
    String getDescription();

}
