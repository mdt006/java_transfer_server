package com.ds.transfer.xiaoyu.vo;

public class LoginEncryptVo {
	private String u;
	private int si;
	private String ut;
	private String p;
	private int it;
	private long ts;
	
	
	/**
	 * 
	 * @param u 用户名
	 * @param si siteId
	 * @param ut 分公司_股东,总代,代理
	 * @param p Pan:会员所属盘口 (A|B|C|D)
	 * @param it It:0表示试用账号，1表示非试用账号
	 * @param ts 时间戳timeStamp(1493961651),精确到秒
	 */
	public LoginEncryptVo(String u, int si, String ut, String p, int it, long ts) {
		super();
		this.u = u;
		this.si = si;
		this.ut = ut;
		this.p = p;
		this.it = it;
		this.ts = ts;
	}
	
	
	
	public LoginEncryptVo() {
		super();
	}



	public String getU() {
		return u;
	}
	public void setU(String u) {
		this.u = u;
	}
	public int getSi() {
		return si;
	}
	public void setSi(int si) {
		this.si = si;
	}
	public String getUt() {
		return ut;
	}
	public void setUt(String ut) {
		this.ut = ut;
	}
	public String getP() {
		return p;
	}
	public void setP(String p) {
		this.p = p;
	}
	public int getIt() {
		return it;
	}
	public void setIt(int it) {
		this.it = it;
	}
	public long getTs() {
		return ts;
	}
	public void setTs(long ts) {
		this.ts = ts;
	}
}