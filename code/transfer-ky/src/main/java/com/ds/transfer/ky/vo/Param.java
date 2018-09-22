package com.ds.transfer.ky.vo;

public class Param implements java.io.Serializable{
	private static final long serialVersionUID = -6383825937395698311L;
	private String s;
	private String account;
	private String money;
	private String orderid;
	private String ip;
	private String lineCode;
	
	//登录
	public Param(String s, String account, String money, String orderid,
			String ip, String lineCode) {
		super();
		this.s = s;
		this.account = account;
		this.money = money;
		this.orderid = orderid;
		this.ip = ip;
		this.lineCode = lineCode;
	}
	
	//转账
	public Param(String s, String account, String money, String orderid) {
		super();
		this.s = s;
		this.account = account;
		this.money = money;
		this.orderid = orderid;
	}
	//查余额
	public Param(String s, String account, String lineCode) {
		super();
		this.s = s;
		this.account = account;
		this.account = account;
		this.lineCode = lineCode;
	}
	
	public Param() {
		super();
	}

	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getLineCode() {
		return lineCode;
	}

	public void setLineCode(String lineCode) {
		this.lineCode = lineCode;
	}
}
