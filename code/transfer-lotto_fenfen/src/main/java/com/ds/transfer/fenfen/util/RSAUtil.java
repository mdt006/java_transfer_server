package com.ds.transfer.fenfen.util;
import org.apache.commons.codec.binary.Base64;
import java.math.BigInteger;
import java.security.KeyFactory;   
import java.security.PublicKey;   
import java.security.spec.RSAPublicKeySpec;   
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;



public class RSAUtil {      
	
    public static byte[] encrypt(String mingwenStr) {   
        try {   
        	String module = PropsUtil.getProperty("module.key");
        	String exponentString = PropsUtil.getProperty("exponentString");
            byte[] modulusBytes = Base64.decodeBase64(module);
            byte[] exponentBytes = Base64.decodeBase64(exponentString);
            BigInteger modulus = new BigInteger(1, modulusBytes);   
            BigInteger exponent = new BigInteger(1, exponentBytes);   
  
            RSAPublicKeySpec rsaPubKey = new RSAPublicKeySpec(modulus, exponent);   
            KeyFactory fact = KeyFactory.getInstance("RSA");   
            PublicKey pubKey = fact.generatePublic(rsaPubKey);   
  
            Cipher cipher = Cipher.getInstance("RSA");   
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);   
  
            byte[] cipherData = cipher.doFinal(new String(mingwenStr).getBytes());   
            return cipherData;   
        } catch (Exception e) {   
            e.printStackTrace();   
        }   
        return null;   
  
    }   
    
    
    /**
    * @Title: encryptByPublicKey 
    * @Package com.ds.transfer.fenfen.util
    * @Description: TODO(官方彩RSA加密工具) 
    * @param @param data
    * @param @return    设定文件 
    * @return byte[]    返回类型 
    * @date: 2017年10月27日 下午1:43:23  
    * @author: leo 
    * @version V1.0
    * @Copyright: 2017 鼎泰科技 Inc. All rights reserved. 
    * 注意：本内容仅限于鼎泰科技有限公司内部传阅，禁止外泄以及用于其他的商业目
     */
    public static byte[] encryptByPublicKey(byte[] data) {
        byte[] result = null;
        try {
        	String module = PropsUtil.getProperty("module.key");
            //byte[] bytes = decryptBase64(module);
            byte[] bytes = Base64.decodeBase64(module);
            // 取得公钥
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = factory.generatePublic(keySpec);
            // 对数据加密
            Cipher cipher = Cipher.getInstance(factory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            result = cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    
    /**
     * BASE64 解密
     * @param key 需要解密的字符串
     * @return 字节数组
     * @throws Exception
     */
    /*public static byte[] decryptBase64(String key) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(key);
    }*/
  
   /* public static byte[] Dencrypt(byte[] encrypted) {   
        try {   
            byte[] expBytes = Base64.decode(delement);   
            byte[] modBytes = Base64.decode(module);   
  
            BigInteger modules = new BigInteger(1, modBytes);   
            BigInteger exponent = new BigInteger(1, expBytes);   
  
            KeyFactory factory = KeyFactory.getInstance("RSA");   
            Cipher cipher = Cipher.getInstance("RSA");   
  
            RSAPrivateKeySpec privSpec = new RSAPrivateKeySpec(modules, exponent);   
            PrivateKey privKey = factory.generatePrivate(privSpec);   
            cipher.init(Cipher.DECRYPT_MODE, privKey);   
            byte[] decrypted = cipher.doFinal(encrypted);   
            return decrypted;   
        } catch (Exception e) {   
            e.printStackTrace();   
        }   
        return null;   
    }   */
}  