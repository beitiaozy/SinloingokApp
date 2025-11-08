package com.sinloingok.app.models.user;

import lombok.Data;

/**
 * 充值订单
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
public class RechargeOrder {

    private static final long serialVersionUID = 706876086093138501L;

    private Long id;
    private String code;
    private Long userId;
    private Long rechargeId;
    private String onlyCode;
    private String type;
    private Long addressId;
    private String status;
    private String createTime;
    private Float money;
    private Float awards;

}
