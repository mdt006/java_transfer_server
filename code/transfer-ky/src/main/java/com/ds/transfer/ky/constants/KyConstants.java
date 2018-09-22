package com.ds.transfer.ky.constants;

/**
 * ky 常量
 * @author leo
 */
public class KyConstants {
	public final static String KY ="ky";
	//KY登录指令
	public final static String LOGIN ="0";
	//KY查询余额指令
	public final static String BALANCE ="1";
	//KY转账转入指令
	public final static String IN ="2";
	//KY转账转出指令
	public final static String OUT ="3";
	//KY查询转账订单指令
	public final static String CHECK ="4";
	//参数连接符
	public final static String KY_PARAM_JOIN = "&";
	
	public static String DES_KEY;  			//APi帐号
	public static String MD5_KEY; 			//APi密码

	public static String print() {
		return "DES_KEY="+DES_KEY+",MD5_KEY="+MD5_KEY;
	}
}
