package com.ds.transfer.common.vo;
import java.io.Serializable;
import com.ds.transfer.record.entity.ApiInfoEntity;

/**
 * 创建会员存在参数
 * 
 * @author jackson
 *
 */
public class UserParam implements Serializable {
	private static final long serialVersionUID = 4622340834592967436L;
	private ApiInfoEntity entity;	//转账配置实体
	private String username;		//会员名称
	private String oddtype;			//盘口
	private String cur;				//币种
	private String isDemo;          //isDemo 是ag专用: 0=试玩, 1=真钱账号
	private String loginUrl;		//登陆url
	private String playerIp;		//玩家ip
	private String terminal;		//玩家终端

	public UserParam() {
	}

	public UserParam(ApiInfoEntity entity, String username, String oddtype, String cur, String isDemo,String loginUrl) {
		this.entity = entity;
		this.username = username;
		this.oddtype = oddtype;
		this.cur = cur;
		this.isDemo = isDemo;
		this.loginUrl=loginUrl;
	}
	
	public UserParam(String username,String cur,String isDemo,String playerIp,String terminal,ApiInfoEntity entity) {
		this.entity = entity;
		this.username = username;
		this.cur = cur;
		this.isDemo = isDemo;
		this.playerIp = playerIp;
		this.terminal = terminal;
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
	public String getOddtype() {
		return oddtype;
	}
	public void setOddtype(String oddtype) {
		this.oddtype = oddtype;
	}
	public String getCur() {
		return cur;
	}
	public void setCur(String cur) {
		this.cur = cur;
	}
	public String getIsDemo() {
		return isDemo;
	}
	public void setIsDemo(String isDemo) {
		this.isDemo = isDemo;
	}
	public String getLoginUrl() {
		return loginUrl;
	}
	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}
	public String getPlayerIp() {
		return playerIp;
	}
	public void setPlayerIp(String playerIp) {
		this.playerIp = playerIp;
	}
	public String getTerminal() {
		return terminal;
	}
	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}
}