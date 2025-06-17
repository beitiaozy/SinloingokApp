package com.sinloingok.app.models.user;

import lombok.Data;

/**
 *  這是一個終端流水記錄表，應該把所有ID轉化為實際需要記錄的值
 * 用户余额变动记录
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
public class UserMoneyRecord {

    /**
     *
     */
    private static final long serialVersionUID = -2203242414978157353L;

    private Long id;
    private Long userId;
    private Long shopId;  //場地所有者的用戶ID
    private Long addressId;
    private String remark;
    private Long orderId;
    private Float money;// 充值金額
    private Float awards;// 當次充值贈送額度
    private Float balance;// 充值後的餘額
    private Float extMoney; // 充值後的贈送額度
    private String payCode;
    private Float spreadAwards;
    private String type;
    private Long rechargeLogId;
    private String createTime;
    private String message;

}
