package com.sinloingok.app.util;

import com.sinloingok.app.constant.SysConstant;
import com.sinloingok.app.kits.MD5;

import java.util.Random;

/**
 * 编码器
 * @author kaptenkabu
 * 所属权归 Sinloingok
 *
 */
public class NextCodeUtils {
	
	static int c = 1;
	static long now, last;
	
	public static long next() {
		last = now = System.currentTimeMillis() * 100000;
		now = System.currentTimeMillis() * 100000;
		if (last == now) {
			now += (c++ % 100000);
		} else {
			last = now;
		}
		return now;
	}
	

	/**
	 * 生成手机验证码
	 * @return
	 */
	public static String nextVercode() {
		Random random = new Random();
		String code = String.valueOf(random.nextInt(8999) + 1000);
		return code;
	}
	
	/**
	 * 生成用户登录Token验证随机码
	 * @return
	 */
	public static String nextRandomCode(){
		Random random = new Random();
		String code = String.valueOf(random.nextInt(899999) + 100000);
		return code;
	}



	/**
	 * 获得Token
	 *
	 * @param user_id
	 * @param random_code
	 */
	public static String getToken(Long user_id, String random_code) {
		String text = SysConstant.COMMON_CODE + user_id + random_code;
		try {
			return MD5.md5(text, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}














