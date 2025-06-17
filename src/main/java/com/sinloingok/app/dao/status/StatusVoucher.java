package com.sinloingok.app.dao.status;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户卡券状态枚举类
 * 定义卡券的所有可能状态及其显示文本
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
public enum StatusVoucher implements BaseEnum{

    /**
     * 有效期中 - 卡券处于有效状态，可以使用
     */
    ACTIVE("有效期中", "卡券有效，可以使用"),

    /**
     * 已用完 - 卡券已被完全使用（针对次卡）
     */
    USED_UP("已用完", "卡券已全部使用完毕"),

    /**
     * 已过期 - 卡券已超过有效期
     */
    EXPIRED("已过期", "卡券已超过有效期"),

    /**
     * 已暂停 - 卡券被暂时停用（针对月卡）
     */
    SUSPENDED("已暂停", "卡券已被暂停使用"),

    /**
     * 未激活 - 卡券尚未激活使用
     */
    INACTIVE("未激活", "卡券尚未激活"),

    /**
     * 已退款 - 卡券已申请退款
     */
    REFUNDED("已退款", "卡券已退款"),

    /**
     * 已冻结 - 卡券因异常操作被冻结
     */
    FROZEN("已冻结", "卡券已冻结,请明日再使用");

    // 状态显示文本
    private String genreName;
    private String description;

    /**
     * 构造函数
     * @param genreName 显示文本
     * @param description 状态描述
     */
    StatusVoucher(String genreName, String description) {
        this.genreName = genreName;
        this.description = description;
    }

    /**
     * 获取枚举代码（用于数据库存储和序列化）
     */
    @JsonValue
    public String getCode() {
        return name();
    }

    /**
     * 获取状态显示文本
     * @return 显示文本
     */
    public String getGenreName() {
        return genreName;
    }

    /**
     * 获取状态描述
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据状态代码获取枚举实例
     * @param code 状态代码
     * @return 对应的枚举实例，如果找不到则返回null
     */
    public static StatusVoucher fromCode(String code) {
        if (code == null) {
            return null;
        }

        try {
            return StatusVoucher.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 根据状态代码获取显示文本
     * @param code 状态代码
     * @return 显示文本，如果找不到则返回代码本身
     */
    public static String getGenreName(String code) {
        StatusVoucher status = fromCode(code);
        return status != null ? status.getGenreName() : code;
    }

    /**
     * 根据状态代码获取描述
     * @param code 状态代码
     * @return 状态描述，如果找不到则返回空字符串
     */
    public static String getDescription(String code) {
        StatusVoucher status = fromCode(code);
        return status != null ? status.getDescription() : "";
    }

    /**
     * 检查状态代码是否有效
     * @param code 状态代码
     * @return true-有效，false-无效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }

    /**
     * 判断状态是否表示卡券可用
     * @param code 状态代码
     * @return true-可用，false-不可用
     */
    public static boolean isAvailable(String code) {
        return ACTIVE.name().equals(code);
    }

    /**
     * 判断状态是否表示卡券不可用
     * @param code 状态代码
     * @return true-不可用，false-可用
     */
    public static boolean isUnavailable(String code) {
        StatusVoucher status = fromCode(code);
        return status != null &&
                (status == USED_UP || status == EXPIRED ||
                        status == SUSPENDED || status == FROZEN);
    }

    /**
     * 获取所有可用状态的代码数组
     * @return 可用状态代码数组
     */
    public static String[] getAvailableStatuses() {
        return new String[]{ACTIVE.name()};
    }

    /**
     * 获取所有状态的代码数组
     * @return 所有状态代码数组
     */
    public static Map<String, String> getAllStatuses() {
        Map<String, String> status = new HashMap<>();
        StatusVoucher[] values = values();
        for (int i = 0; i < values.length; i++) {
            status.put(values[i].name(), values[i].getGenreName());
        }
        return status;
    }

    /**
     * 获取所有状态的显示文本数组
     * @return 所有状态显示文本数组
     */
    public static String[] getAllStatusName() {
        StatusVoucher[] values = values();
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i].getGenreName();
        }
        return result;
    }

    @Override
    public String toString() {
        return this.name() + "(" + genreName + ")";
    }
}