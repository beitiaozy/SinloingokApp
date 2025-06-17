package com.sinloingok.app.dao.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 交易流水类型枚举
 */
public enum TransactionFlowType {
    
    /**
     * 充值
     */
    RECHARGE(1, "充值", "用户账户充值操作"),
    
    /**
     * 消费
     */
    CONSUME(2, "消费", "用户消费扣款操作"),
    
    /**
     * 后台修改
     */
    ADMIN_MODIFY(3, "后台修改", "管理员后台调整金额操作"),
    
    /**
     * 月卡充值
     */
    MONTHLY_CARD_RECHARGE(4, "月卡充值", "用户购买或续费月卡操作"),
    
    /**
     * 次卡充值
     */
    TIMES_CARD_RECHARGE(5, "次卡充值", "用户充值次卡操作"),
    
    /**
     * 优惠券购买
     */
    COUPON_PURCHASE(6, "优惠券购买", "用户购买优惠券操作");

    private final int code;
    private final String name;
    private final String description;

    TransactionFlowType(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据编码获取枚举实例
     */
    public static TransactionFlowType fromCode(int code) {
        for (TransactionFlowType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的交易流水类型编码: " + code);
    }

    /**
     * 根据编码获取枚举实例（安全版本，返回Optional）
     */
    public static TransactionFlowType getByCodeSafe(int code) {
        for (TransactionFlowType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的交易流水类型编码: " + code);
    }

    /**
     * 验证编码是否有效
     */
    public static boolean isValidCode(int code) {
        for (TransactionFlowType type : values()) {
            if (type.getCode() == code) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有编码列表
     */
    public static List<Integer> getAllCodes() {
        return Arrays.stream(values())
                .map(TransactionFlowType::getCode)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有枚举值的映射表（code -> name）
     */
    public static Map<Integer, String> getCodeNameMap() {
        Map<Integer, String> map = new HashMap<>();
        for (TransactionFlowType type : values()) {
            map.put(type.getCode(), type.getName());
        }
        return map;
    }

    @Override
    public String toString() {
        return "TransactionFlowType{" +
                "code=" + code +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}