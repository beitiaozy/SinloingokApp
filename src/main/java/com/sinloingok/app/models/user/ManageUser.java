package com.sinloingok.app.models.user;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统用户
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Data
@NoArgsConstructor
public class ManageUser {

    /**
     *
     */
    private static final long serialVersionUID = 5527426709811746505L;


    private Long id;
    private Long applyUserId;
    private String randomCode;
    private String mobile;
    private String username;
    private String password;
    private String nickname;
    private String jpushcode;
    private Float money;
    private String status;
    private String type;
    private String lastLoginTime;
    private String updateTime;
    private String createTime;
    private Long roleId;
    private String cityId;
    private Float num;
    private Long agentId;
    private Float rate;
    private Long userId;
    private String staffCard;
    private Integer dailiRate;

    public ManageUser(String mobile, String password) {
        this.setMobile(mobile);
        setPassword(password);
    }

}
