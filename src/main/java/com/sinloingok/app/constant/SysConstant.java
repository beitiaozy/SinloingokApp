package com.sinloingok.app.constant;

/**
 * 系统常量
 * @author kaptenkabu
 * 所属权归 Sinloingok
 *
 */
public interface SysConstant {

	/**生成Token的公用码*/
	public final String COMMON_CODE = "YUANYUANGODDESS";
	
	/**头像上传位置*/
	public final String AVATAR_BASE = "/uploads/avatar";
	
	/**默认头像*/
	public static String DEFAULT_AVATAR = "/uploads/imgs/default/avatar.jpg";

	public static long DEFAULT_ADDRESS = 4L;

	/**
	 * 接口反馈代码
	 * @author kaptenkabu
	 * 所属权归 Sinloingok
	 *
	 */
	interface ResultCode {

		public static final String SUCCESS = "000";
		public static final String ERROR = "999";

	}
}
