package com.sinloingok.app.constant;

import com.google.common.collect.Lists;
import java.util.*;

/**
 * SignalTopology — 信號拓撲關係定義（1..8 編號制，已反轉）
 *
 * 功能映射：
 *  1=泡沫(PM)，2=洗手(XS)，3=清水2(QS2)，4=吹氣(CQ)，
 *  5=鍍膜(DM)，6=關機(GJ)，7=吸塵(XC)，8=清水1(QS1)
 */
public final class SignalTopology {

    private SignalTopology() {}

    /** NetSite 設備數量（1..8） */
    public static final int NETSITE_COUNT = 8;
    /** 每設備通道數量（1..8） */
    public static final int CHANNEL_COUNT = 8;

    /** PMKZSB 控制設備邏輯名稱 */
    public static final String PMKZSB_DEVICE_CODE = "PMKZSB";

    // —— 通道命名（名稱與新編號保持一致）——
    public static final String CH_PM_1  = "CH_PM_1";   // 1：泡沫
    public static final String CH_XS_2  = "CH_XS_2";   // 2：洗手
    public static final String CH_QS2_3 = "CH_QS2_3";  // 3：清水2
    public static final String CH_CQ_4  = "CH_CQ_4";   // 4：吹氣
    public static final String CH_DM_5  = "CH_DM_5";   // 5：鍍膜
    public static final String CH_GJ_6  = "CH_GJ_6";   // 6：關機
    public static final String CH_XC_7  = "CH_XC_7";   // 7：吸塵
    public static final String CH_QS1_8 = "CH_QS1_8";  // 8：清水1

    /** 功能代號 → 設備編號 */
    private static final Map<String, Integer> FUNCTION_CHNUM_MAP;
    static {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put(CH_PM_1, 1);
        m.put(CH_XS_2, 2);
        m.put(CH_QS2_3, 3);
        m.put(CH_CQ_4, 4);
        m.put(CH_DM_5, 5);
        m.put(CH_GJ_6, 6);
        m.put(CH_XC_7, 7);
        m.put(CH_QS1_8, 8);
        FUNCTION_CHNUM_MAP = Collections.unmodifiableMap(m);
    }

    /** 設備編號 → 功能中文 */
    public static final Map<Integer, String> DEVICE_FUNCTION_ALIAS;
    static {
        Map<Integer, String> m = new LinkedHashMap<>();
        m.put(1, "泡沫");
        m.put(2, "洗手");
        m.put(3, "清水2");
        m.put(4, "吹氣");
        m.put(5, "鍍膜");
        m.put(6, "關機");
        m.put(7, "吸塵");
        m.put(8, "清水1");
        DEVICE_FUNCTION_ALIAS = Collections.unmodifiableMap(m);
    }

    /** 設備編號 → 功能拼音代號（已匹配新的常量名稱） */
    private static final Map<Integer, String> CHNUM_FUNCTION_MAP;
    static {
        Map<Integer, String> m = new LinkedHashMap<>();
        m.put(1, CH_PM_1);
        m.put(2, CH_XS_2);
        m.put(3, CH_QS2_3);
        m.put(4, CH_CQ_4);
        m.put(5, CH_DM_5);
        m.put(6, CH_GJ_6);
        m.put(7, CH_XC_7);
        m.put(8, CH_QS1_8);
        CHNUM_FUNCTION_MAP = Collections.unmodifiableMap(m);
    }

    /** 所有通道名稱列表 */
    private static final List<String> ALL_CHANNEL_NAMES = Lists.newArrayList(
            CH_PM_1, CH_XS_2, CH_QS2_3, CH_CQ_4,
            CH_DM_5, CH_GJ_6, CH_XC_7, CH_QS1_8
    );

    /** 所有設備編號列表 */
    private static final List<Integer> ALL_CHANNEL_NUMBERS = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8);

    // ========== 通道獲取方法 ==========

    /**
     * 根據通道編號獲取功能代號
     */
    public static String getFunctionCode(int chNum) {
        return CHNUM_FUNCTION_MAP.get(chNum);
    }

    /**
     * 根據功能代號獲取通道編號
     */
    public static Integer getChannelNumber(String functionCode) {
        return FUNCTION_CHNUM_MAP.get(functionCode);
    }

    /**
     * 根據通道編號獲取功能中文名稱
     */
    public static String getFunctionName(int chNum) {
        return DEVICE_FUNCTION_ALIAS.get(chNum);
    }

    /**
     * 根據功能代號獲取功能中文名稱
     */
    public static String getFunctionName(String functionCode) {
        Integer chNum = getChannelNumber(functionCode);
        return chNum != null ? DEVICE_FUNCTION_ALIAS.get(chNum) : null;
    }

    /**
     * 獲取所有通道編號
     */
    public static List<Integer> getAllChannelNumbers() {
        return new ArrayList<>(ALL_CHANNEL_NUMBERS);
    }

    /**
     * 獲取所有功能代號
     */
    public static List<String> getAllFunctionCodes() {
        return new ArrayList<>(ALL_CHANNEL_NAMES);
    }

    /**
     * 獲取所有功能中文名稱
     */
    public static List<String> getAllFunctionNames() {
        return new ArrayList<>(DEVICE_FUNCTION_ALIAS.values());
    }

    /**
     * 檢查通道編號是否有效
     */
    public static boolean isValidChannelNumber(int chNum) {
        return chNum >= 1 && chNum <= NETSITE_COUNT;
    }

    /**
     * 檢查功能代號是否有效
     */
    public static boolean isValidFunctionCode(String functionCode) {
        return FUNCTION_CHNUM_MAP.containsKey(functionCode);
    }

    /**
     * 根據通道編號獲取完整的通道信息
     */
    public static ChannelInfo getChannelInfo(int chNum) {
        if (!isValidChannelNumber(chNum)) {
            return null;
        }
        return new ChannelInfo(
                chNum,
                getFunctionCode(chNum),
                getFunctionName(chNum)
        );
    }

    /**
     * 根據功能代號獲取完整的通道信息
     */
    public static ChannelInfo getChannelInfo(String functionCode) {
        Integer chNum = getChannelNumber(functionCode);
        return chNum != null ? getChannelInfo(chNum) : null;
    }

    /**
     * 獲取所有通道的完整信息
     */
    public static List<ChannelInfo> getAllChannelInfos() {
        List<ChannelInfo> infos = new ArrayList<>();
        for (int i = 1; i <= NETSITE_COUNT; i++) {
            infos.add(getChannelInfo(i));
        }
        return infos;
    }

    // ========== 通道信息類 ==========

    /**
     * 通道信息封裝類
     */
    public static class ChannelInfo {
        private final int channelNumber;
        private final String functionCode;
        private final String functionName;

        public ChannelInfo(int channelNumber, String functionCode, String functionName) {
            this.channelNumber = channelNumber;
            this.functionCode = functionCode;
            this.functionName = functionName;
        }

        public int getChannelNumber() {
            return channelNumber;
        }

        public String getFunctionCode() {
            return functionCode;
        }

        public String getFunctionName() {
            return functionName;
        }

        @Override
        public String toString() {
            return String.format("ChannelInfo{channelNumber=%d, functionCode='%s', functionName='%s'}",
                    channelNumber, functionCode, functionName);
        }
    }

    // —— 臨時映射（保持原樣）——
    private static final List<String> list = Lists.newArrayList(
            "0090B2B6215D", "0090D500245B", "0090D5000F10", "0090D500242A", "0090D5000F4A"
    );

    public static String tempWscNetSiteMapping(int chNum){
        return list.get(chNum);
    }

    /** 臨時泡沫控制設備的OnlyCode */
    public static String tempPmkzsbOnlyCode(){
        return list.get(0);
    }

    /** 臨時獲取泡沫總控端口號 */
    public static int tempWscNetSitePmChNum(String wscOnlyCode){
        return list.indexOf(wscOnlyCode);
    }
}