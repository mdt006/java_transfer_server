package com.ds.transfer.common.vo;

import java.io.Serializable;

import com.ds.transfer.record.entity.ApiInfoEntity;

/**
 * 登录公共参数
 * 
 * @author jackson
 *
 */
public class LoginParam implements Serializable {

	private static final long serialVersionUID = 6922806211425138551L;

	private ApiInfoEntity entity;
	private String username;//用户名
	private String billno;//订单号
	private String gameType;//游戏类型
	private String isDemo;//是否试玩
	private String pageSite;//
	private String action;//
	private String accType;//ag; 分分彩--> dcUserTree; 经典彩--> userTree
	private String oddType;//限红
	private String cur;//货币
	private String line;//线路; 分分彩--> dcCustomerId 
	private String language;//语言
	private String lottoTray;
	private String lottoType;
	private String gamekind;//bbin; 
	private String gamecode;//bbin; 
	private String terminal;//终端
	private String playerIp;
	//Mg 新增登录参数
	private String bankingUrl;
	private String lobbyUrl;
	private String logoutRedirectUrl;
	//经典彩新增登录
	private String loginAssignType;
	private String lid;
	private String platformURL;
	//新增幸运彩地址
	private String reportUrl;
	private String loginUrl;  //彩票新增多线路
	private String passWord;  //PT新增参数
	private String appId;	  //MG新增参数
	private String ip;
	private String isFlash;
	
	
	public LoginParam(ApiInfoEntity entity, String username) {
		this.entity = entity;
		this.username = username;
	}
	public LoginParam(ApiInfoEntity entity, String username,String logoutRedirectUrl) {
		this.entity = entity;
		this.username = username;
		this.logoutRedirectUrl=logoutRedirectUrl;
	}
	public ApiInfoEntity getEntity() {
		return entity;
	}
	public void setEntity(ApiInfoEntity entity) {
		this.entity = entity;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getBillno() {
		return billno;
	}
	public void setBillno(String billno) {
		this.billno = billno;
	}
	public String getGameType() {
		return gameType;
	}
	public void setGameType(String gameType) {
		this.gameType = gameType;
	}
	public String getLoginAssignType() {
		return loginAssignType;
	}
	public void setLoginAssignType(String loginAssignType) {
		this.loginAssignType = loginAssignType;
	}
	public String getIsDemo() {
		return isDemo;
	}
	public void setIsDemo(String isDemo) {
		this.isDemo = isDemo;
	}
	public String getPageSite() {
		return pageSite;
	}
	public void setPageSite(String pageSite) {
		this.pageSite = pageSite;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getAccType() {
		return accType;
	}
	public void setAccType(String accType) {
		this.accType = accType;
	}
	public String getCur() {
		return cur;
	}
	public void setCur(String cur) {
		this.cur = cur;
	}
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getLottoTray() {
		return lottoTray;
	}
	public void setLottoTray(String lottoTray) {
		this.lottoTray = lottoTray;
	}
	public String getLottoType() {
		return lottoType;
	}
	public void setLottoType(String lottoType) {
		this.lottoType = lottoType;
	}
	public String getOddType() {
		return oddType;
	}
	public void setOddType(String oddType) {
		this.oddType = oddType;
	}
	public String getGamekind() {
		return gamekind;
	}
	public void setGamekind(String gamekind) {
		this.gamekind = gamekind;
	}
	public String getGamecode() {
		return gamecode;
	}
	public void setGamecode(String gamecode) {
		this.gamecode = gamecode;
	}
	public String getTerminal() {
		return terminal;
	}
	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}
	public String getBankingUrl() {
		return bankingUrl;
	}
	public void setBankingUrl(String bankingUrl) {
		this.bankingUrl = bankingUrl;
	}
	public String getLobbyUrl() {
		return lobbyUrl;
	}
	public void setLobbyUrl(String lobbyUrl) {
		this.lobbyUrl = lobbyUrl;
	}
	public String getLogoutRedirectUrl() {
		return logoutRedirectUrl;
	}
	public void setLogoutRedirectUrl(String logoutRedirectUrl) {
		this.logoutRedirectUrl = logoutRedirectUrl;
	}
	public String getLid() {
		return lid;
	}
	public void setLid(String lid) {
		this.lid = lid;
	}
	public String getPlatformURL() {
		return platformURL;
	}
	public void setPlatformURL(String platformURL) {
		this.platformURL = platformURL;
	}
	public String getReportUrl() {
		return reportUrl;
	}
	public void setReportUrl(String reportUrl) {
		this.reportUrl = reportUrl;
	}
	public String getLoginUrl() {
		return loginUrl;
	}
	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}
	public String getPassWord() {
		return passWord;
	}
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPlayerIp() {
		return playerIp;
	}
	public void setPlayerIp(String playerIp) {
		this.playerIp = playerIp;
	}
	public String getIsFlash() {
		return isFlash;
	}
	public void setIsFlash(String isFlash) {
		this.isFlash = isFlash;
	}
	
}