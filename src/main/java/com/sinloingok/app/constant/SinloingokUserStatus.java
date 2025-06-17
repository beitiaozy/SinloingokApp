package com.sinloingok.app.constant;

public interface SinloingokUserStatus {
    /**
     * 会员等级
     * @author kaptenkabu
     * 所属权归 Sinloingok
     *
     */
    interface UserLevel {

        public static final String NORMAL = "普通会员";
    }

    /**
     * 用户角色ID
     * @author kaptenkabu
     * 所属权归 Sinloingok
     *
     */
    interface RoleID {

        /**代理角色ID*/
        public final Long DAILI = 68L;

        /**商户角色ID*/
        public final Long SHANGHU = 70L;

    }

    /**
     * 后台使用者状态
     * @author kaptenkabu
     * 所属权归 Sinloingok
     *
     */
    interface ManageUserStatus {

        public final String USING = "启用";
        public final String NO_USING = "暂不启用";
    }
}
