package com.ds.transfer.money.util;

import java.io.IOException;
import java.util.Properties;

public class PropsUtil {

	public static Properties props = new Properties();

	static {
		try {
			props.load(ClassLoader.getSystemResourceAsStream("money.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String key) {
		return props.getProperty(key);
	}

}
