package com.ds.transfer.bbin.constants;

public interface BbinConstants {

	String KEYB = "NngjW675ZM";
	String BALANCE_KEY = "v9OY2m74";
	String CHECK_TRANSFER = "Gw320Iu2D";
	String CREATE_MEMBER_KEY = "Qwmyu515";
	String LOGIN_KEY = "a3S5O9";
	String PLAY_GAME_KEY = "eJQ4l";
	String BBIN = "bbin";
	String WEB_SITE = "kkw910";
	String STATE_SUCCESS = "11100";
	String BBIN_URL = "http://180.150.154.103/dtapi/app/bbin";
	String BBIN_URL_LOGIN = "http://888.s1116.com/app/WebService/JSON/display.php";
	String LIVE_ID = "11";
	String LANG = "zh-cn";

	interface function {
		String TRANSFER = "/Transfer";
		String QUERY_BALANCE = "/CheckUsrBalance";
		String CREATE_MEMBER = "/CreateMember";
		String CHECK_TRANSFER = "/CheckTransfer";

		String LOGIN = "/Login";
		String LOGIN_SINGLE = "/Login2";
		String PLAY_GAME = "/PlayGame";
		String H5_GAME = "/ForwardGameH5By5";
	}
}
