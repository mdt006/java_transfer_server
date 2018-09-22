package com.ds.transfer.common.util;

import java.math.BigInteger;   
import java.security.KeyFactory;   
import java.security.PrivateKey;   
import java.security.PublicKey;   
import java.security.spec.RSAPrivateKeySpec;   
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;
import org.apache.commons.codec.binary.Base64;


public class RSAUtil {   
  
    private static String module = "z85/em9EvzccdNHnxYQb/cSjl6FTR43X6Czqa6+oSd0UgAuCtzXaGixo7RbiMnzSWCJKxY26nrsucwwBEld4t5zLSCRDKhT+9Y1zKyzyVdSO2ac5WDXyuNiXOzZ+oz0T0Haf6kZnh7sI/9AdWTPkM1eK5Vo4uKKIDrzduQBnJKE=";   
    private static String exponentString = "AQAB";   
    private static String delement = "RleRwnXxjUwyCiWMK0U+AESD2r2wFno8Z5D30gKjKx1PMPL63qOhIRxdP3O0oWwEG6RK4Dh+ikxV8tzn4fTDHgcEIWTxHRGMolQTrYH6StDj07Cg/VtWFBQ7jlNQLVEqxgWGZmSlpBVZYn0yOmXhpjfPx43FQil6BnIDdBthcOc=";   
    private static String encryptString = "GSEg9mrRXyoPopVrNkVkc/FgQxfFx3eSSqJ8R6rqndIIStyZ+j92XFyDwaN8t27RYWemBPL6y7GnJDswXJO7gMBALfuATUynieuqwZWuYhE4pAUGsYwNuSV8+kf/Z46O+eiwjlOkzsxYTw7hY/ZyU1JQo8bA1GNPCc7tk2wv18g=";   
    /**   
     * @param args   
     */   
    public static void main(String[] args) {
    	String mingwen = "chenhailong";
        byte[] en = encrypt(mingwen); 
        //密文
        System.out.println(Base64.encodeBase64(en));
        //密文
        byte[] enTest = null;   
        try {   
            enTest = Base64.decodeBase64(encryptString);
        } catch (Exception e) {
            e.printStackTrace();   
        }   
//        System.out.println(enTest.length);   
//        System.out.println(en.length);   
        System.out.println(new String(Dencrypt(en)));   
        System.out.println(new String(Dencrypt(enTest)));   
    }   
  
    public static byte[] encrypt(String mingwenStr) {   
        try {   
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
  
    public static byte[] Dencrypt(byte[] encrypted) {   
        try {   
            byte[] expBytes = Base64.decodeBase64(delement);
            byte[] modBytes = Base64.decodeBase64(module);
  
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
    }   
}  