package com.ds.transfer.ky.vo;

public class BaseParams implements java.io.Serializable{
	private static final long serialVersionUID = 9003210561472197658L;
	
	private String agent;
	private String param;
	private String timestamp;
	private String key;
	
	public BaseParams(String agent, String param, String timestamp, String key) {
		super();
		this.agent = agent;
		this.param = param;
		this.timestamp = timestamp;
		this.key = key;
	}

	public BaseParams() {
		super();
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
