package com.ds.transfer.lmg.vo;

/**
 * ds 登录
 * 
 * @author jackson
 *
 */
public class LoginVo {
	private String hashCode;
	private String command;
	private Param params;

	public LoginVo() {
	}

	public LoginVo(String hashCode, String command) {
		this.hashCode = hashCode;
		this.command = command;
	}

	public LoginVo(String hashCode, String command, Param params) {
		this.hashCode = hashCode;
		this.command = command;
		this.params = params;
	}

	public class Param {
		private String username; //合作伙伴平台的用户ID,字符串长度少于或者等于20位, 由字母和数字组成
		private String password; //合作伙伴平台的用户原始密码，要求ＭＤ５加密, 长度为32位
		private String currency; //合作伙伴的用户ID所使用的币别
		private String nickname; //合作伙伴的用户昵称, 字符串长度少于或者等于20位
		private String language; //游戏语言(CN,HK,EN,TH,VN)|(CN,EN)
		private int line;        //线路(可选参数) 　参数(1, 2, …)  默认线路为1, 注:具体线路参数ID以游戏平台为准
		private String gametype; //游戏类型
		private String tipStatus;//小费功能状态（CLOSE 关闭，不设置均为开启）

		/** 大厅登录 */
		public Param(String username, String password, String currency, String nickname, String language, int line, String gametype,String tipStatus) {
			this.username = username;
			this.password = password;
			this.currency = currency;
			this.nickname = nickname;
			this.language = language;
			this.line = line;
			this.gametype = gametype.toUpperCase();
			this.tipStatus = tipStatus;
		}
		
		public Param(String username,String password){
			this.username = username;
			this.password = password;
		}
		
		public String getGametype() {
			return gametype;
		}
		public void setGametype(String gametype) {
			this.gametype = gametype;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public String getCurrency() {
			return currency;
		}
		public void setCurrency(String currency) {
			this.currency = currency;
		}
		public String getNickname() {
			return nickname;
		}
		public void setNickname(String nickname) {
			this.nickname = nickname;
		}
		public String getLanguage() {
			return language;
		}
		public void setLanguage(String language) {
			this.language = language;
		}
		public int getLine() {
			return line;
		}
		public void setLine(int line) {
			this.line = line;
		}
		public String getTipStatus() {
			return tipStatus;
		}
		public void setTipStatus(String tipStatus) {
			this.tipStatus = tipStatus;
		}
	}

	public String getHashCode() {
		return hashCode;
	}
	public void setHashCode(String hashCode) {
		this.hashCode = hashCode;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public Param getParams() {
		return params;
	}
	public void setParams(Param params) {
		this.params = params;
	}
}
