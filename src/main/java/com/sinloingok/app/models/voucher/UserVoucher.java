package com.sinloingok.app.models.voucher;

import com.sinloingok.app.dao.status.GenericRecharge;
import com.sinloingok.app.dao.status.StatusVoucher;
import com.sinloingok.app.util.DateUtils;
import lombok.Data;

/**
 * 用户的洗车券记录
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
public class UserVoucher {

    /**
     *
     */
    private static final long serialVersionUID = 706876086093138501L;


    private Long id;
    private Long userId;//關聯用戶
    private Long addressId;//適用場地
    private String payCode;// 支付碼
    private String status;// 狀態   有效期中，已用完，已過期
    private int amount; //次數
    private Float totalMoney;
    private String createTime;//創建時間
    private String expireDate;//有效期截止日
    private String finishTime;// 用完時間
    private String useTime;// 開始使用日期 -- 月卡/次卡每次使用後累加一日 確保每日只能用一次
    private String type;//類型，月卡，次卡和優惠券 Monthly Card /session card / Coupon card

    private String addressName;
    /**
     * 获取状态显示文本
     * @return 状态显示文本
     */
    public String getTypeName() {
        return GenericRecharge.fromCode(this.type).getGenreName();
    }

    /**
     * 获取状态描述
     * @return 状态描述
     */
    public String getTypeDescription() {
        return GenericRecharge.fromCode(this.type).getDescription();
    }


    /**
     * 获取状态显示文本
     * @return 状态显示文本
     */
    public String getStatusName() {
        return StatusVoucher.getGenreName(this.status);
    }

    /**
     * 获取状态描述
     * @return 状态描述
     */
    public String getStatusDescription() {
        int days = DateUtils.daysBetween2(useTime, DateUtils.todayDate(0));
        return days < 0 && StatusVoucher.ACTIVE.getCode().equals(status) ? StatusVoucher.FROZEN.getDescription() : StatusVoucher.getDescription(this.status);
    }

    /**
     * 检查卡券是否可用
     * @return true-可用，false-不可用
     */
    public boolean isAvailable() {
        return StatusVoucher.isAvailable(this.status);
    }

    public void minusAmount(){
        amount --;
        if(amount == 0){
            setStatus(StatusVoucher.USED_UP.name());
            setFinishTime(DateUtils.curTime());
        }
        setUseTime(DateUtils.todayDate(1));
    }
}
