package com.ds.transfer.og.vo;


/**
 * og 转账所需参数
 * 
 * @author jackson
 *
 */
//@Data
public class OgTransferVo {

	private String agent;

	private String username;

	/**
	 * U.S. Dollar(USD), 
	 * RMB(RMB), 
	 * Malaysia Dollar(MYR), 
	 * Korea Won(KOW), 
	 * Singapore Dollar(SGD), 
	 * Hong Kong Dollar(HKD)
	 */
	private String moneysort;//货币类型

	private String password;

	private String billno;

	private String type;

	private String credit;

	private String limit;//限制游戏Game Limit Ex:1,1,1,1,1,1,1,1,1,1,1,1,1, and 1 is can ,0 is not can.13 numbers

	private String limitvideo;//百家乐/龙虎/骰宝/翻摊

	private String limitroulette;//轮盘的

	private String domain;//域名

	/**
	 * 	1: 视讯
	 *	2: 体育
	 *	3: 彩票
	 *	4: 电子游戏
	 *	11:新平台(明升)
	 *	21:手机体育
	 */
	private String gametype;

	private String gamekind;

	private String platformname;//平台名称:Oriental,ibc,ag,opus

	private String lang;//zh中文，en英文，jp日文，kr 韩文

	private String method;

	public OgTransferVo() {
	}

	/** 转账 */
	public OgTransferVo(String agent, String username, String password, String billno, String type, String credit, String method) {
		this.agent = agent;
		this.username = username;
		this.password = password;
		this.billno = billno;
		this.type = type;
		this.credit = credit;
		this.method = method;
	}

	/** CheckAndCreateAccount（检测并创建游戏帐户） */
	public OgTransferVo(String agent, String username, String moneysort, String password, String limit, String limitvideo, String limitroulette, String method) {
		this.agent = agent;
		this.username = username;
		this.moneysort = moneysort;
		this.password = password;
		this.limit = limit;
		this.limitvideo = limitvideo;
		this.limitroulette = limitroulette;
		this.method = method;
	}

	/** GetBalance（查询余额） */
	public OgTransferVo(String agent, String username, String password, String method) {
		this.agent = agent;
		this.username = username;
		this.password = password;
		this.method = method;
	}

	/** TransferGame（进入游戏）登录 */
	public OgTransferVo(String agent, String username, String password, String domain, String gametype, String gamekind, String platformname, String lang, String method) {
		this.agent = agent;
		this.username = username;
		this.password = password;
		this.domain = domain;
		this.gametype = gametype;
		this.gamekind = gamekind;
		this.platformname = platformname;
		this.lang = lang;
		this.method = method;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getMoneysort() {
		return moneysort;
	}

	public void setMoneysort(String moneysort) {
		this.moneysort = moneysort;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getBillno() {
		return billno;
	}

	public void setBillno(String billno) {
		this.billno = billno;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCredit() {
		return credit;
	}

	public void setCredit(String credit) {
		this.credit = credit;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public String getLimitvideo() {
		return limitvideo;
	}

	public void setLimitvideo(String limitvideo) {
		this.limitvideo = limitvideo;
	}

	public String getLimitroulette() {
		return limitroulette;
	}

	public void setLimitroulette(String limitroulette) {
		this.limitroulette = limitroulette;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getGametype() {
		return gametype;
	}

	public void setGametype(String gametype) {
		this.gametype = gametype;
	}

	public String getGamekind() {
		return gamekind;
	}

	public void setGamekind(String gamekind) {
		this.gamekind = gamekind;
	}

	public String getPlatformname() {
		return platformname;
	}

	public void setPlatformname(String platformname) {
		this.platformname = platformname;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
}