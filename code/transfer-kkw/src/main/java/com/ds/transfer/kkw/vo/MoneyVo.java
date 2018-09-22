package com.ds.transfer.kkw.vo;
/**
 * kkw transfer vo
 * @author leo
 */
public class MoneyVo {

	private String username;
	private String password; // md5
	private String ref;
	private String amount;
	private String desc;

	public MoneyVo(String username, String password, String ref,String desc,String amount) {
		this.username = username;
		this.password = password;
		this.ref = ref;
		this.amount = amount;
		this.desc = desc;
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

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
