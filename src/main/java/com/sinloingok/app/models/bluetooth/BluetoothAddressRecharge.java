package com.sinloingok.app.models.bluetooth;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 场地充值活动列表
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
@NoArgsConstructor
public class BluetoothAddressRecharge {

    private static final long serialVersionUID = -6009680277019724808L;


    private Long id;
    private String title;

    private Float money;
    /**
     * DealCombo 优惠套餐, MonthCombo 优惠套餐，表示次数
     *
     * routine=常规, newcomer=新人, holiday=节假日 表示赠送金额
     */
    private Float awards;

    /**
     * 剩余天数
     */
    private String remainingDays ;

    /**
     * 有效日期
     */
    private String expirationDate;

    /**
     *  Recharge genre充值类型: routine=常规, newcomer=新人, holiday=节假日, exclusive=专属活动, DealCombo 优惠套餐, MonthCombo 优惠套餐
     */
    private String type;

    private Long addressId;

    private int enable;

    private String userLevel;

    private String createTime;

    public void setType(String type) {
        this.type = type.toUpperCase();
    }
}
