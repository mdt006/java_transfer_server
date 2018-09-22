package com.ds.transfer.pt.constants;

public class PtConstants {
	public static String PT;
	public static String PT_URL;
	public static String CRT_PWD;
	
	public static String CREATE_MEMBER = "/create";     //创建用户接口
	public static String TRANSFER_IN = "/deposit";		//转账转入接口
	public static String TRANSFER_OUT = "/withdraw";	//转账转出接口
	public static String QUERY_BALANCE = "/balance";	//查询余额接口
	public static String CHECK = "/checktransaction";   //转账校验接口
	public static String REPLACE_PASSWORD = "/update";	//修改密码接口
	
	public static String KIOSKNAME = "DTJISHU";     	//公司名称
	public static String CUSTOM02 = "WIN88ENTITY";		//客户号
	
	public static String getPT() {
		return PT;
	}
	public static void setPT(String pT) {
		PT = pT;
	}
	public static String getPT_URL() {
		return PT_URL;
	}
	public static void setPT_URL(String pT_URL) {
		PT_URL = pT_URL;
	}
	public static String getCRT_PWD() {
		return CRT_PWD;
	}
	public static void setCRT_PWD(String cRT_PWD) {
		CRT_PWD = cRT_PWD;
	}
	
	
}
