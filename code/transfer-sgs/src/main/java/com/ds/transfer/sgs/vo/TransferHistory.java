package com.ds.transfer.sgs.vo;
import com.opencsv.bean.CsvBindByName;

public class TransferHistory implements java.io.Serializable{
	private static final long serialVersionUID = -5491183992377055998L;
	
	@CsvBindByName(column="txid")
	private String txid;
	
	@CsvBindByName(column="timestamp")
	private String timestamp;
	
	@CsvBindByName(column="userid")
	private String userid;
	
	@CsvBindByName(column="username")
	private String username;
	
	@CsvBindByName(column="amt")
	private String amt;
	
	@CsvBindByName(column="postbal")
	private String postbal;
	
	@CsvBindByName(column="cur")
	private String cur;

	
	public String getTxid() {
		return txid;
	}

	public void setTxid(String txid) {
		this.txid = txid;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getUserid() {
		return userid;
	}


	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAmt() {
		return amt;
	}

	public void setAmt(String amt) {
		this.amt = amt;
	}

	public String getPostbal() {
		return postbal;
	}

	public void setPostbal(String postbal) {
		this.postbal = postbal;
	}

	public String getCur() {
		return cur;
	}

	public void setCur(String cur) {
		this.cur = cur;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	@Override
	public String toString() {
		return "TransferHistory [txid=" + txid + ", timestamp=" + timestamp
				+ ", userid=" + userid + ", username=" + username + ", amt="
				+ amt + ", postbal=" + postbal + ", cur=" + cur + "]";
	}
}
