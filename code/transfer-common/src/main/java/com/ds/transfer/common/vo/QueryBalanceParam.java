package com.ds.transfer.common.vo;
import java.io.Serializable;
import com.ds.transfer.record.entity.ApiInfoEntity;

/**
 * 查询余额所需参数
 * @author jackson
 */
public class QueryBalanceParam implements Serializable {

	private static final long serialVersionUID = 8634718150541610215L;
	
	private ApiInfoEntity entity;
	private String username;
	private String cur;
	private String agent;
	private String ip;
	private String playerIp;
	private String terminal;
	private String isDemo;

	public QueryBalanceParam() {
	}
	public QueryBalanceParam(ApiInfoEntity entity, String username,
			String cur,String ip,String playerIp,String terminal,String isDemo) {
		this.entity = entity;
		this.username = username;
		this.cur = cur;
		this.ip = ip;
		this.playerIp = playerIp;
		this.terminal = terminal;
		this.isDemo = isDemo;
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
	public String getCur() {
		return cur;
	}
	public void setCur(String cur) {
		this.cur = cur;
	}
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
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
	public String getTerminal() {
		return terminal;
	}
	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}
	public String getIsDemo() {
		return isDemo;
	}
	public void setIsDemo(String isDemo) {
		this.isDemo = isDemo;
	}
}
