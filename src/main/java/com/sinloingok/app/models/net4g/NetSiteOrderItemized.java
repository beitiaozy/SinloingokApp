package com.sinloingok.app.models.net4g;

import com.sinloingok.app.util.DateUtils;
import com.sinloingok.app.util.ns.DeviceControlService2;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
public class NetSiteOrderItemized {
    /** 合并容忍窗口（秒）：防止因时间抖动导致的边界重复计费 */
    private static final int MERGE_TOLERANCE_SECONDS = 2;

    private Long id;
    private String ruleCode;
    private String ruleName;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private Integer minBillDuration = Integer.MAX_VALUE;
    private Integer maxBillDuration = Integer.MIN_VALUE;
    private BigDecimal maxPrice = BigDecimal.ZERO;
    private String payCode;
    private String onlyCode;
    private String chCode;
    private Integer onlyCodePort = 0;
    private Integer totalDuration = 0;

    private Integer perUseDuration = 0;
    private String perUseStartTime;
    private String createTime;
    private String updateTime;

    public NetSiteOrderItemized(NetSiteBillRule rule, String payCode, String onlyCode){
        this.setRuleCode(rule.getRuleCode());
        this.setRuleName(rule.getRuleName());
        this.setUnitPrice(rule.getUnitPrice());
        this.setMinBillDuration(rule.getMinBillDuration());
        this.setMaxBillDuration(rule.getMaxBillDuration());
        this.setMaxPrice(rule.getMaxPrice());
        this.setPayCode(payCode);
        this.setOnlyCode(onlyCode);
        this.setOnlyCodePort(rule.getChannelNo());
        setCreateTime(DateUtils.curTime());
        setUpdateTime(DateUtils.curTime());
    }

    public String  runCalculate(){
        if(perUseStartTime == null){
            ruleBeginCalculate();
            return DeviceControlService2.COMMAND_OPEN;
        }else {
            ruleFinishCalculate();
            return DeviceControlService2.COMMAND_CLOSE;
        }
    }

    // ====== 开始信号处理 ======
    public void ruleBeginCalculate() {
        final String now = DateUtils.curTime();

        // 若上次有未结算段，先按最小计费补结算（防丢包、补齐理论结束时间）
        if (perUseStartTime != null) {
            return;
           // setUpdateTime(addPerUserDuration(0));
        }
        // 判断 now 是否仍落在“已计费窗口”内（或刚越过一点点）
        if (isWithinChargedWindow(now)) {
            // 仍在已计费窗口内：不再追加最小计费，直接把新的开始对齐到上次理论结束时间
            setPerUseStartTime(updateTime);
            setPerUseDuration(0); // 不重复触发 min
        } else {
            // 已明显超出已计费窗口：开启一个新的最小计费段
            setPerUseStartTime(now);
            setPerUseDuration(getMinBillDuration());
            setUpdateTime(now);
        }

    }

    // ====== 结束信号处理 ======
    public void ruleFinishCalculate() {
        long seconds = 0L;
        if (perUseStartTime != null) {
            seconds = DateUtils.secondsDifference(DateUtils.curTime(), getPerUseStartTime());
            // 结束早于开始（例如开始被对齐到未来的 updateTime）则不计增量
            seconds = Math.max(0L, seconds);
            // 为应对异常抖动，可设单次上限（可按需调大/调小）
            seconds = Math.min(seconds, 30);
        }
        String actualTime = addPerUserDuration((int) seconds);
        setUpdateTime(actualTime);
    }

    /**
     * 工具：当前时刻是否落在“已计费窗口”内（或刚越界 MERGE_TOLERANCE_SECONDS 秒内）
     * 逻辑：若 now - updateTime <= 容忍窗口，则视为仍在已计费范围内，应合并而不是新起一段最小计费
     */
    private boolean isWithinChargedWindow(String now) {
        if (updateTime == null) return false;
        long delta = DateUtils.secondsDifference(now, updateTime); // 约等于 now - updateTime
        return delta <= MERGE_TOLERANCE_SECONDS;
    }

    // ====== 按最小计费/实际计费入账 ======
    private String addPerUserDuration(int actualDuration) {
        // 若这段本就保障了 min，则用 max；否则（合并进入的段）可能是 0 或实值
        actualDuration = Math.max(actualDuration, perUseDuration);

        totalDuration += actualDuration;
        totalDuration = Math.min(totalDuration, getMaxBillDuration());

        String start = getPerUseStartTime();
        setPerUseDuration(0);
        setPerUseStartTime(null);
        if(start == null) return DateUtils.curTime();
        // 返回“理论结束时间”用于更新 updateTime
        return DateUtils.actualTimeAddSeconds(start, actualDuration);
    }

    /**
     * 計算該消費明細項目的總金額
     * @return
     */
    public BigDecimal calculateItemAmount(){
        BigDecimal totalCal = unitPrice.multiply(new BigDecimal(totalDuration+ perUseDuration));
        return totalCal.min(maxPrice);
    }

}