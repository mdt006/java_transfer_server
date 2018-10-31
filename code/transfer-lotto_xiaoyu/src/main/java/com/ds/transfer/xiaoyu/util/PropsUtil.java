package com.ds.transfer.xiaoyu.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropsUtil {

	public static Properties props = new Properties();

	static {
		try {
			//props.load(new FileInputStream(new File("resource" + File.separator + "xiaoyu.properties")));
			props.load(ClassLoader.getSystemResourceAsStream("xiaoyu.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String key) {
		return props.getProperty(key);
	}

}
