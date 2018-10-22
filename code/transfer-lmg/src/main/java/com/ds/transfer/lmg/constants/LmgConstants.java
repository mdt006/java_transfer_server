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

	//和用户有关（测试环境）
	final String TEST_USER="http://gbklivesw.iasia99.com/gbkapilivesw/app/api.do";
	//和用户有关（正式环境）
	final String FORMAL_USER="http://gbklive.ppkp88.com/gbkapilive/app/api.do";
	//和钱有关（测试环境）
	final String TEST_MONEY="http://gbktradesw.iasia99.com/gbkapitradesw/app/api.do";
	//和钱有关（正式环境）
	final String FORMAL_MONEY="http://gbktrade.ppkp88.com/gbkapitrade/app/api.do";
	//和记录有关的（测试环境）
	final String TEST_RECORD="http://gbkrecordsw.iasia99.com/gbkapirecordsw/app/api.do";
	//和记录有关的（正式环境）
	final String FORMAL_RECORD="http://gbkrecord.ppkp88.com/gbkapirecord/app/api.do";
}
