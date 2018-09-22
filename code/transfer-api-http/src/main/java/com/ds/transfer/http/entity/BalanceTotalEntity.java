package com.ds.transfer.http.entity;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.ds.transfer.common.service.TransferService;
import com.ds.transfer.common.vo.QueryBalanceParam;
import com.ds.transfer.http.service.SupportTransferService;
import com.ds.transfer.record.entity.ApiInfoEntity;

/**
* @ClassName: BalanceTotalEntity
* @Description: TODO(资金归集实体类)
* @author leo
* @date 2018年3月6日
*/
public class BalanceTotalEntity implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	private Map<String, ApiInfoEntity> siteIdLiveIdMap;
	private Map<String, TransferService<?>> transferServiceMap;
	private Map<String, SupportTransferService<?>> supportServiceMap;
	private Map<String, Object> resultMap;
	private ApiInfoEntity apiInfoEntity;
	private QueryBalanceParam queryParam;
	private CountDownLatch cdl;
	private String isDemo;
	private String credit;
	private String username;
	private String billno;
	private String cur;
	private long id ;  			//转账记录id
	private String ip; 			//客户端ip
	private String playerIp;	//玩家ip
	private String terminal;	//终端
	
	
	public ApiInfoEntity getApiInfoEntity() {
		return apiInfoEntity;
	}
	public void setApiInfoEntity(ApiInfoEntity apiInfoEntity) {
		this.apiInfoEntity = apiInfoEntity;
	}
	public String getIsDemo() {
		return isDemo;
	}
	public void setIsDemo(String isDemo) {
		this.isDemo = isDemo;
	}
	public String getCredit() {
		return credit;
	}
	public void setCredit(String credit) {
		this.credit = credit;
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
	public String getCur() {
		return cur;
	}
	public void setCur(String cur) {
		this.cur = cur;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Map<String, ApiInfoEntity> getSiteIdLiveIdMap() {
		return siteIdLiveIdMap;
	}
	public void setSiteIdLiveIdMap(Map<String, ApiInfoEntity> siteIdLiveIdMap) {
		this.siteIdLiveIdMap = siteIdLiveIdMap;
	}
	public Map<String, TransferService<?>> getTransferServiceMap() {
		return transferServiceMap;
	}
	public void setTransferServiceMap(
			Map<String, TransferService<?>> transferServiceMap) {
		this.transferServiceMap = transferServiceMap;
	}
	public Map<String, SupportTransferService<?>> getSupportServiceMap() {
		return supportServiceMap;
	}
	public void setSupportServiceMap(
			Map<String, SupportTransferService<?>> supportServiceMap) {
		this.supportServiceMap = supportServiceMap;
	}
	public Map<String, Object> getResultMap() {
		return resultMap;
	}
	public void setResultMap(Map<String, Object> resultMap) {
		this.resultMap = resultMap;
	}
	public QueryBalanceParam getQueryParam() {
		return queryParam;
	}
	public void setQueryParam(QueryBalanceParam queryParam) {
		this.queryParam = queryParam;
	}
	public CountDownLatch getCdl() {
		return cdl;
	}
	public void setCdl(CountDownLatch cdl) {
		this.cdl = cdl;
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
}