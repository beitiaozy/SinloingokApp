package com.sinloingok.app.aop;

public enum LoginType {
    /**
     * 无需登录
     */
    ANONYMOUS,
    /**
     * 普通用户登录
     */
    USER,
    /**
     * 管理员登录
     */
    MANAGER,
    /**
     * 代理商登录
     */
    AGENT,
    /**
     * 自定义角色（需配合roles参数）
     */
    CUSTOM
}