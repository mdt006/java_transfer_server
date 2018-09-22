package com.ds.transfer.lmg.constants;


public interface LmgConstants {
	final String GAME_TYPE = "LMG_LIVE";
			
	final String LMG = "lmg";

	//LMG登录指令
	final String LOGIN = "LOGIN";
	
	//LMG修改密码指令
	final String CHANCE_PASSWORD = "CHANGE_PASSWORD";
	
	//LMG获取余额指令
	final String BALANCE = "GET_BALANCE";
	
	//LMG转账转入指令
	final String IN = "DEPOSIT";
	
	//LMG转账转出指令
	final String OUT = "WITHDRAW";
	
	//LMG验证交易号
	final String CHECK = "CHECK_REF";
	
}
