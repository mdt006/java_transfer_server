package com.ds.transfer.xiaoyu.util;
import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.KeyFactory;   
import java.security.PublicKey;   
import java.security.spec.RSAPublicKeySpec;   
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