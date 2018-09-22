package com.ds.transfer.ag.constants;

/**
 * ag 常量
 * 
 * @author jackson
 *
 */
public class  AgConstants {
	
	public static String CREATE_MEMBER_URL;
	public static String LOGIN_URL;
	public static String AG_URL;
	public static String AG_PID;
	public static String AG_MD5_KEY;
	public static String AG_ENCRYPT_KEY;
	public static String AG = "ag";
	public static String AG_PARAM_JOIN = "/\\\\/";
	public static String TRANS_SUCCESS = "0";
	public static String PRE_TRANSFER = "tc";
	public static String CONFIRM_TRANSFER = "tcc";
	public static String QUERY_BALANCE = "gb";
	public static String CREATE_MEMBER = "lg";
	public static String QUERY_ORDER_STATUS = "qos";
	public static String LIVE_ID = "2";
	public static String BUSINESS_FUNCTION = "/doBusiness.do";
	public static String LOGIN_FUNCTION = "/forwardGame.do";
	
	
	public static String getCREATE_MEMBER_URL() {
		return CREATE_MEMBER_URL;
	}

	public static void setCREATE_MEMBER_URL(String cREATE_MEMBER_URL) {
		CREATE_MEMBER_URL = cREATE_MEMBER_URL;
	}

	public static String getLOGIN_URL() {
		return LOGIN_URL;
	}

	public static void setLOGIN_URL(String lOGIN_URL) {
		LOGIN_URL = lOGIN_URL;
	}

	public static String getAG_URL() {
		return AG_URL;
	}

	public static void setAG_URL(String aG_URL) {
		AG_URL = aG_URL;
	}

	public static String getAG_PID() {
		return AG_PID;
	}

	public static void setAG_PID(String aG_PID) {
		AG_PID = aG_PID;
	}

	public static String getAG_MD5_KEY() {
		return AG_MD5_KEY;
	}

	public static void setAG_MD5_KEY(String aG_MD5_KEY) {
		AG_MD5_KEY = aG_MD5_KEY;
	}

	public static String getAG_ENCRYPT_KEY() {
		return AG_ENCRYPT_KEY;
	}

	public static void setAG_ENCRYPT_KEY(String aG_ENCRYPT_KEY) {
		AG_ENCRYPT_KEY = aG_ENCRYPT_KEY;
	}
	
}
