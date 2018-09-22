package com.ds.transfer.sgs.utils;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public class Transfer {
	public static final Map<String,String> cache = new HashMap<String,String>();
	
	public static void main(String[] args) throws Exception {
		Transfer transfer = new Transfer();
		//获取token认证
		System.out.println("开始获取token...");
		String token = transfer.getToken();
		System.out.println("token = " + token +"\n");
		
		//获取转账历史记录
		System.out.println("调用接口获取转账历史记录...");
		String tranferHistory = transfer.tranferHistory();
		String[] historyList = tranferHistory.split("\r\n");
		
		for (int i = 1; i < historyList.length; i++) {
			String[] lineArr = historyList[i].split(",");
			System.out.println("转账列表："+historyList[i]);
			for (int j = 0; j < lineArr.length; j++) {
				String fields = lineArr[j];
				System.out.println(fields);
			}
		}
		
		//查询余额
		System.out.println("调用接口获取余额...");
		String balance = transfer.getBalance();
		System.out.println("balance = " + balance +"\n");
		
		//转账转入余额
		System.out.println("调用接口转账转入余额...");
		String creditWallet = transfer.getWalletCredit();
		System.out.println("creditWallet = " + creditWallet +"\n");
		
		//转账转出余额
		System.out.println("调用接口转账转出余额...");
		String creditDebit = transfer.getWalletDebit();
		System.out.println("creditDebit = " + creditDebit +"\n");
		
		//登陆
		System.out.println("调用接口登陆...");
		String login = transfer.login();
		System.out.println("login = " + login +"\n");
		
		//注销，登出
		System.out.println("调用接口会员登出...");
		String deauthorize = transfer.deauthorize();
		System.out.println("deauthorize = " + deauthorize +"\n");
	}
	
	private String tranferHistory() throws Exception {
		String url="http://sctrapi.sbuat.com/api/report/transferhistory?userid=cs498772011&includetestplayers=true&startdate=2018-06-29T08:58:00%2B00:00&enddate=2018-06-29T20:58:00%2B00:00";
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("signature", "DSznM7Aw1388UzS2TdVCiQpPirw=");
		param.put("sgsDate", "2018-06-29T08:58:00Z");
		System.out.println("signature="+cache.get("signature") +",sgsDate="+cache.get("UTCTimeStr"));
		String sendGet = RequestUtils.sendGet(url,param);
		System.out.println(sendGet);
		return sendGet;
	}
	
	private String login() throws Exception {
		String url="http://staging.tgpasia.com/sc88desktopT5D?token=jlhFxO5d2C8IhNJF2B7VkPzk7J1t11xP0zHniyIqKMdLoqkiX5Hncj06qoWXnXO7u5PklkPn9HWwOXTyCbMUKEQASn6B3rWjTWQ3ZtTlIPJkZNPcpIK99rNZ3OzFhuPiZ";
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("signature", cache.get("signature"));
		param.put("sgsDate", cache.get("UTCTimeStr"));
		
		String sendGet = RequestUtils.sendGet(url,param);
		return sendGet;
	}
	
	private String getWalletDebit() throws Exception {
		String url = "http://sctrapi.sbuat.com/api/wallet/debit";
		Map<String,Object> requestParam = new HashMap<String,Object>();
		requestParam.put("userid","12345");
		requestParam.put("amt",15000.0);
		requestParam.put("cur","RMB");
		requestParam.put("txid","5bb70123-ae1f-4e3e-0c40-9f35a98d3896");
		requestParam.put("timestamp","2018-06-07T17:40:20.6155542+00:00");
		requestParam.put("desc",null);
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("signature", cache.get("signature"));
		param.put("sgsDate", cache.get("UTCTimeStr"));
		param.put("requestParam", JSONObject.toJSON(requestParam));
		return RequestUtils.sendPost(url,param,"utf-8");
	}
	
	//转账转入
	public String getWalletCredit() throws Exception{
		String url = "http://sctrapi.sbuat.com/api/wallet/credit";
		Map<String,Object> requestParam = new HashMap<String,Object>();
		requestParam.put("userid","12345");
		requestParam.put("amt",0.0);
		requestParam.put("cur","RMB");
		requestParam.put("txid","5bb70123-ae1f-4e3e-9c40-8635a9853e96");
		requestParam.put("timestamp","2018-06-06T16:34:20.6155542+00:00");
		requestParam.put("desc",null);
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("signature", cache.get("signature"));
		param.put("sgsDate", cache.get("UTCTimeStr"));
		param.put("requestParam", JSONObject.toJSON(requestParam));
		return RequestUtils.sendPost(url,param,"utf-8");
	}
	
	//查询余额
	public String getBalance() throws Exception{
		String url = "http://sctrapi.sbuat.com/api/player/balance?userid=12345&cur=RMB";
		String client_secret = "FrFBfKlUHvaK9jFMhM6YfEyclWSVljAnc5019KaxaMEi";
		String UTCTimeStr = DateUtils.getUTCTime();
		StringBuilder StringToSign = new StringBuilder();
		StringToSign.append(client_secret).append(UTCTimeStr);
		String signature = SHA1Utils.encrypt(client_secret,StringToSign.toString());
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("signature", signature);
		param.put("sgsDate",UTCTimeStr);
		
		return RequestUtils.sendGet(url,param);
	}
	
	//退出，注销，登出
	public String deauthorize() throws Exception{
		String url = "http://sctrapi.sbuat.com/api/player/deauthorize";
		
		Map<String,Object> requestParam = new HashMap<String,Object>();
		requestParam.put("userid","12345");
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("signature", cache.get("signature"));
		param.put("sgsDate", cache.get("UTCTimeStr"));
		param.put("requestParam", JSONObject.toJSON(requestParam));
		return RequestUtils.sendPost(url,param,"utf-8");
	}
	
	//获取token
	public String getToken() throws Exception{
		String url = "http://sctrapi.sbuat.com/api/player/authorize";
		String client_secret = "FrFBfKlUHvaK9jFMhM6YfEyclWSVljAnc5019KaxaMEi";
		String UTCTimeStr = DateUtils.getUTCTime();
		System.out.println(UTCTimeStr);
		StringBuilder StringToSign = new StringBuilder();
		StringToSign.append(client_secret).append(UTCTimeStr);
		System.out.println(StringToSign.toString());
		String signature = SHA1Utils.encrypt(client_secret,StringToSign.toString());
		cache.put("UTCTimeStr", UTCTimeStr);
		cache.put("signature", signature);
		
		Map<String,Object> requestParam = new HashMap<String,Object>();
		requestParam.put("ipaddress","123.123.1.95");
		requestParam.put("username","john");
		requestParam.put("userid", "12345");
		requestParam.put("lang", "zh-CN");
		requestParam.put("cur", "RMB");
		requestParam.put("betlimitid", 1);
		requestParam.put("platformtype", 1);
		requestParam.put("istestplayer", false);
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("signature", signature);
		param.put("sgsDate", UTCTimeStr);
		param.put("requestParam", JSONObject.toJSON(requestParam));
		
		return RequestUtils.sendPost(url,param,"utf-8");
	}
}
