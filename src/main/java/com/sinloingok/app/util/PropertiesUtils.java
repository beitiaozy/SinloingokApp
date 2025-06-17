package com.sinloingok.app.util;

import java.io.IOException;
import java.util.Properties;

/**
 * 属性文件辅助
 * @author kaptenkabu
 * 所属权归 Sinloingok
 *
 */
public class PropertiesUtils {

	public static Properties getProperties() {
		Properties p = new Properties();
		try {
			p.load(PropertiesUtils.class.getClassLoader().getResourceAsStream("RestAPIConfig.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}
}






