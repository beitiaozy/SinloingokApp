package com.sinloingok.app.models.voucher;

import lombok.Data;

/**
 * 用户的洗车券记录
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
public class UserVoucherRecord {

    /**
     *
     */
    private static final long serialVersionUID = 706876086093138501L;


    private Long id;
    private Long userId;
    private Long addressId;
    private String voucher_id;
    private Float totalMoney;//實際使用金額
    private int amount;
    private String useTime;// 使用時間
    private String type;

}
