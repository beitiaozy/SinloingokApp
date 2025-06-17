package com.sinloingok.app.dtos;

import java.io.Serializable;

public class StandardRtnDto<T> implements Serializable {
    private static final long serialVersionUID = 4475329876461122752L;

    // 常用状态码
    public static final String SUCCESS_CODE = "000";
    public static final String ERROR_CODE = "999";
    public static final String PARAM_ERROR_CODE = "400";
    public static final String UNAUTHORIZED_CODE = "401";
    public static final String FORBIDDEN_CODE = "403";
    public static final String NOT_FOUND_CODE = "404";
    public static final String SERVER_ERROR_CODE = "500";

    private String code = "";
    private T data;

    // 构造函数保持不变
    public StandardRtnDto(String code, T data) {
        if (code.endsWith("000")) {
            this.code = "000";
        } else {
            this.code = code;
        }
        this.data = data;
    }

    public StandardRtnDto(String code) {
        if (code.endsWith("000")) {
            this.code = "000";
        } else {
            this.code = code;
        }
        this.data = null;
    }

    // ============= 成功响应方法 =============

    /**
     * 成功响应(无数据)
     */
    public static <T> StandardRtnDto<T> success() {
        return new StandardRtnDto<>(SUCCESS_CODE, null);
    }

    /**
     * 成功响应(带数据)
     */
    public static <T> StandardRtnDto<T> success(T data) {
        return new StandardRtnDto<>(SUCCESS_CODE, data);
    }

    /**
     * 成功响应(自定义消息)
     */
    public static <T> StandardRtnDto<T> success(String message) {
        return new StandardRtnDto(SUCCESS_CODE, message);
    }

//    /**
//     * 成功响应(自定义消息和数据)
//     */
//    public static <T> StandardRtnDto<String> success(String message, T data) {
//        return new StandardRtnDto<T>(SUCCESS_CODE, new ResponseWrapper<>(message, data));
//    }

    // ============= 错误响应方法 =============

    /**
     * 通用错误响应
     */
    public static <T> StandardRtnDto<T> error() {
        return new StandardRtnDto(ERROR_CODE, "操作失败");
    }

    /**
     * 错误响应(自定义消息)
     */
    public static <T> StandardRtnDto<T> error(String message) {
        return new StandardRtnDto(ERROR_CODE, message);
    }

    /**
     * 错误响应(自定义错误码和消息)
     */
    public static <T> StandardRtnDto<T> error(String code, String message) {
        return new StandardRtnDto(code, message);
    }

    /**
     * 参数错误响应
     */
    public static StandardRtnDto paramError() {
        return new StandardRtnDto(PARAM_ERROR_CODE, "参数错误");
    }

    /**
     * 参数错误响应(自定义消息)
     */
    public static <T> StandardRtnDto<T> paramError(String message) {
        return new StandardRtnDto(PARAM_ERROR_CODE, message);
    }

    /**
     * 未授权响应
     */
    public static <T> StandardRtnDto<T> unauthorized() {
        return new StandardRtnDto(UNAUTHORIZED_CODE, "未授权");
    }

    /**
     * 禁止访问响应
     */
    public static <T> StandardRtnDto<T> forbidden() {
        return new StandardRtnDto(FORBIDDEN_CODE, "禁止访问");
    }

    /**
     * 资源未找到响应
     */
    public static <T> StandardRtnDto<T> notFound() {
        return new StandardRtnDto(NOT_FOUND_CODE, "资源未找到");
    }

    /**
     * 服务器错误响应
     */
    public static <T> StandardRtnDto<T> serverError() {
        return new StandardRtnDto(SERVER_ERROR_CODE, "服务器内部错误");
    }

    // ============= 判断方法 =============

    /**
     * 判断响应是否成功
     */
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(this.code);
    }

    /**
     * 判断响应是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }

    // ============= getter/setter =============
    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }
}