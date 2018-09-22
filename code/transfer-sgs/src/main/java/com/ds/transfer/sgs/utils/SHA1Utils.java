package com.ds.transfer.sgs.utils;
import java.nio.charset.Charset;
import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SHA1Utils {
	private static  Logger logger = LoggerFactory.getLogger(SHA1Utils.class);
	
	public static String encrypt(String key, String idString)throws SignatureException {
		try {
			String algorithm = "HmacSHA1";
			Charset charset = Charset.forName("utf-8");
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(),algorithm);
			Mac mac = Mac.getInstance(algorithm);
			mac.init(signingKey);
			return new String(Base64.encodeBase64(mac.doFinal(idString.getBytes(charset))), charset);
		} catch (Exception e) {
			logger.error("SHA1 加密异常！key = "+key+",error info",e);
			throw new SignatureException("Failed to generate HMAC : "+ e.getMessage());
		}
	}
}
