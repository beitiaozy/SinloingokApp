package com.sinloingok.app.dao.status;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum GenericRecharge implements BaseEnum{

    /**
     * 常规充值
     */
    ROUTINE("常规", "常规充值"),

    /**
     * 新人专享
     */
    NEWCOMER( "新人", "新人专享优惠"),

    /**
     * 节假日活动
     */
    HOLIDAY("节假日", "节假日特别活动"),

    /**
     * 专属活动
     */
    EXCLUSIVE("专属活动", "特定用户专属活动"),

    /**
     * 优惠套餐
     */
    DEAL_COMBO( "次卡套餐", "次卡优惠组合，有效期60天"),
    /**
     * 优惠套餐
     */
    MONTH_COMBO( "月卡套餐", "月卡优惠组合，有效期30天");

    private String genreName;
    private String description;

   private static List<GenericRecharge> BALANCE_RECHARGE_TYPES
            = Arrays.asList(GenericRecharge.NEWCOMER, GenericRecharge.HOLIDAY, GenericRecharge.ROUTINE);

    private static final List<GenericRecharge> COMBO_RECHARGE_TYPES
            = Arrays.asList(GenericRecharge.DEAL_COMBO, GenericRecharge.MONTH_COMBO);
    GenericRecharge(String genreName, String description) {
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
     * 获取显示名称（用于前端展示）
     */
    public String getGenreName() {
        return genreName;
    }

    /**
     * 获取详细描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * Jackson反序列化方法
     * 支持从字符串转换为枚举
     */
    @JsonCreator
    public static GenericRecharge fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        // 支持大小写不敏感的比较
        String normalizedCode = code.toUpperCase();

        for (GenericRecharge genre : values()) {
            if (genre.name().equals(normalizedCode)) {
                return genre;
            }
        }
        return null;
    }

    /**
     * 根据显示名称获取枚举
     */
    public static GenericRecharge fromGenreName(String genreName) {
        if (genreName == null || genreName.trim().isEmpty()) {
            return null;
        }

        for (GenericRecharge genre : values()) {
            if (genre.genreName.equals(genreName)) {
                return genre;
            }
        }

        throw new IllegalArgumentException("未知的充值类型显示名称: " + genreName);
    }

    /**
     * 检查代码是否有效
     */
    public boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        try {
            return name().equals(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isBalanceRechargeStrategy(String code){
        GenericRecharge rechargeType = GenericRecharge.fromCode(code.toUpperCase());
        return BALANCE_RECHARGE_TYPES.contains(rechargeType);
    }

    public static boolean isComboRechargeStrategy(String code){
        if(StringUtils.isEmpty(code)) return false;
        GenericRecharge rechargeType = GenericRecharge.fromCode(code.toUpperCase());
        return COMBO_RECHARGE_TYPES.contains(rechargeType);
    }

    /**
     * 获取所有枚举代码
     */
    public static String[] getAllCodes() {
        GenericRecharge[] values = values();
        String[] codes = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            codes[i] = values[i].name();
        }
        return codes;
    }

    /**
     * 获取所有显示名称
     */
    public static Map<String, String> getAllGenreMapping() {
        Map<String, String> result = new HashMap<>();
        GenericRecharge[] values = values();
        String[] names = new String[values.length];
        for (GenericRecharge genericEnum : values) {
            result.put(genericEnum.name(), genericEnum.genreName);
        }
        return result;
    }
}
