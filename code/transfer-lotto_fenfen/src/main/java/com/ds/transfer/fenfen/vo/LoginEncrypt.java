package com.ds.transfer.fenfen.vo;
/**
 * ds 登录
 */
//@Data
public class LoginEncrypt {
	
	public LoginEncrypt() {}
	
	private String lid;
	private String param;
	private String encrypt;
	private String clientType;
	private String platformURL;
	
	public LoginEncrypt(String param, String encrypt,String lid,String clientType,String platformURL) {
		super();
		this.param = param;
		this.encrypt = encrypt;
		this.lid=lid;
		this.clientType=clientType;
		this.platformURL=platformURL;
	}
	public String getLid() {
		return lid;
	}
	public void setLid(String lid) {
		this.lid = lid;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public String getEncrypt() {
		return encrypt;
	}
	public void setEncrypt(String encrypt) {
		this.encrypt = encrypt;
	}
	public String getClientType() {
		return clientType;
	}
	public void setClientType(String clientType) {
		this.clientType = clientType;
	}
	public String getPlatformURL() {
		return platformURL;
	}
	public void setPlatformURL(String platformURL) {
		this.platformURL = platformURL;
	}
}
