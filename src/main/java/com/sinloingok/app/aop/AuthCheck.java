package com.sinloingok.app.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {
    /**
     * 认证类型（默认需要普通用户登录）
     */
    LoginType value() default LoginType.ANONYMOUS;
    
    /**
     * 自定义角色（当type=CUSTOM时生效）
     */
    String[] roles() default {};
    
    /**
     * 是否允许临时用户上下文（优先级高于常规登录）
     */
    boolean allowTempUser() default false;
}