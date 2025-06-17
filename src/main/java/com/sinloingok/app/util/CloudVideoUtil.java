package com.sinloingok.app.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 	云音响工具类
 * @author kaptenkabu
 * 所属权归 Sinloingok
 *
 */
public class CloudVideoUtil {

	public static void playVideo(String id, String version, String message) throws Exception {
		String url = "https://speaker.17laimai.cn/notify.php";
		Map<String, String> params = new HashMap<>();
		params.put("id", id);
		params.put("token", "HK1668588373");
		params.put("version", version);
		params.put("message", message);
		NetUtils.http_get(url, params);
	}

	public static void main(String[] args) throws Exception {
		playVideo("2042305186015", "2", "测试播放");
	}
}
