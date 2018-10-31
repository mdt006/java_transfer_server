package com.ds.transfer.ky.listener;
import java.io.FileInputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ds.transfer.ky.constants.KyConstants;

/**
 * spring 初始化需要准备些数据
 * @author leo
 */
@Component
public class KyInit {
	private static final Logger logger = LoggerFactory.getLogger(KyInit.class);
	public static Properties props = new Properties();

	static {
		try {
			logger.info("KyInit start!");
			props.load(new FileInputStream("resource/ky.properties"));
			initKyConstants();
			logger.info(KyConstants.print());
		} catch (Exception e) {
			logger.error("KyInit init error",e);
		}
	}
	
	static void initKyConstants(){
		KyConstants.DES_KEY = props.getProperty("DES_KEY");
		KyConstants.MD5_KEY = props.getProperty("MD5_KEY");
	}
}



