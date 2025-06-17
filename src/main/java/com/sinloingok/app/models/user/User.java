package com.sinloingok.app.models.user;

import com.sinloingok.app.models.fund.UserAddressAccount;
import lombok.Data;

/**
 * 用户实体类
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
public class User {

    private static final long serialVersionUID = 706876086093138501L;

    private Long id;
    private String openId;
    private String sessionKey;
    private String username;
    private String mobile;
    private String password;
    private String randomCode;
    private String avatar;
    private String nickname;
    private Float balance; // 用户充值余额
    private Float money;   // 平台显示合计金额
    private Float extMoney; // 赠送金额
    private Long roleId;
    private String level;
    private String status;
    private String lastLoginIp;
    private String createTime;
    private String lastLoginTime;
    private String newRechargeTime;
    private String devId;

    private UserAddressAccount currentAccount;

}