package com.ds.transfer.sgs.constants;


public interface SgsConstants {
	//SGS获取token
	final String AUTHORIZE = "/api/player/authorize";
	
	//SGS获取余额指令
	final String BALANCE = "/api/player/balance";

	//SGS转账转入指令
	final String IN = "/api/wallet/credit";

	//SGS转账转出指令
	final String OUT = "/api/wallet/debit";

	//SGS登录指令
	final String LOGIN = "/gamelauncher";
	
	//SGS平台简称
	final String SGS = "sgs";

	//SGS验证交易号
	final String CHECK = "/api/report/transferhistory";
	
}
