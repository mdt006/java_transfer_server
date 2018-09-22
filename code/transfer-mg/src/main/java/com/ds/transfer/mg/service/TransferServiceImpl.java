package com.ds.transfer.mg.service;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.onetwo.common.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.InputSource;

import com.alibaba.fastjson.JSONObject;
import com.ds.msg.TelegramMessage;
import com.ds.transfer.common.constants.SysConstants;
import com.ds.transfer.common.service.CommonTransferService;
import com.ds.transfer.common.service.TransferService;
import com.ds.transfer.common.util.BigDemicalUtil;
import com.ds.transfer.common.util.JSONUtils;
import com.ds.transfer.common.util.StringsUtil;
import com.ds.transfer.common.vo.LoginParam;
import com.ds.transfer.common.vo.QueryBalanceParam;
import com.ds.transfer.common.vo.QueryOrderStatusParam;
import com.ds.transfer.common.vo.TransferParam;
import com.ds.transfer.common.vo.UserParam;
import com.ds.transfer.mg.constants.MgConstants;
import com.ds.transfer.mg.constants.TelegramConstants;
import com.ds.transfer.mg.util.MGUtil;
import com.ds.transfer.mg.util.TimeUtil;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.MgApiUserEntity;
import com.ds.transfer.record.entity.MgApiUserEntityExample;
import com.ds.transfer.record.entity.TransferRecordEntity;
import com.ds.transfer.record.mapper.MgApiUserEntityMapper;
import com.ds.transfer.record.service.TransferRecordService;

@Service
public class TransferServiceImpl extends CommonTransferService implements TransferService<MgApiUserEntity> {
	private static final Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);

	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;

	@Resource(name = "moneyCenterService")
	private TransferService<?> moneyCenterService;

	@Autowired
	private MgApiUserEntityMapper mgApiUserEntityMapper;
	
	@Autowired
	protected RedisTemplate<String, String> redisTemplate;
	
	//小飞机消息组件
	static TelegramMessage telegramMessage = TelegramMessage.getInstance();
	
	
	@Override
	public String transfer(TransferParam transferParam) {
		StringBuilder message = new StringBuilder();
		TransferRecordEntity mgRecord = new TransferRecordEntity();
		String msg = "";
		String token = null;
		try {
			ApiInfoEntity entity = transferParam.getEntity();
			String username = transferParam.getUsername();
			username =MgConstants.MG_PREFIX+MgConstants.PARTNERID+entity.getPrefix() + username;
			
			message.append("MG Transfer 站点：").append(entity.getSiteId());
			message.append(",会员：").append(username);
			message.append(",转账：").append(transferParam.getType().equals("IN")==true ? "DS-->MG":"MG-->DS");
			message.append(",单号:").append(transferParam.getBillno());
			message.append(",金额：").append(transferParam.getCredit()).append("\n");
			//mg 转账记录
			mgRecord = this.transferRecordService.insert(entity.getSiteId(), entity.getLiveId(), transferParam.getTransRecordId(), entity.getPassword(), username, transferParam.getCredit(), transferParam.getBillno(),//
					transferParam.getType(), MgConstants.MG, transferParam.getRemark(), mgRecord);
			logger.info("mg转账记录插入成功,  id = {}", mgRecord.getId());
			token=loginMember(username,entity);
			//转账
			boolean result=transaction(token,MgConstants.PRODUCT,transferParam.getType().equals("IN")?"topup":"withdraw",transferParam.getCredit(),transferParam.getBillno(),message);
			if(result){
				return JSONUtils.map2Json(success("MG transfer success"));
			}
			logger.info("MG transfer fail");
			//失败更新下记录
			mgRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "MG转账异常", mgRecord);
			
			message.insert("MG Transfer".length(),"转账异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-mg transfer",message.toString().replace("&", "*"));
		} catch (Exception e) {
			message.insert("MG Transfer".length(),"转账异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-mg transfer",message.toString().replace("&", "*"));
			logger.error("mg转账异常:", e);
		}
		return JSONUtils.map2Json(maybe("MG转账异常:" + msg));
	}

	
	@Override
	public String queryBalance(QueryBalanceParam param) {
		StringBuilder message = new StringBuilder();
		ApiInfoEntity entity = param.getEntity();
		String username = param.getUsername();
		username =MgConstants.MG_PREFIX+MgConstants.PARTNERID+entity.getPrefix() + username;
		
		message.append("MG getPlayerBalance 站点：").append(entity.getSiteId());
		message.append(",会员：").append(username);
		
		String token=loginMember(username, param.getEntity());
		String balance=getPlayerBalance(token,message);
		if(balance==null){
			return JSONUtils.map2Json(failure("MG getPlayerBalance  is null"));
		}
		Map<String,Object> resultMap=success("MG queryBalance  sucess");
		resultMap.put("balance", balance);
		//正则判断值是否标准金额
		if(BigDemicalUtil.checkoutMoney(balance)){
			//查询到的余额添加到缓存，用于批量查询,缓存7天自动清除
			this.redisTemplate.opsForValue().set(entity.getLiveId()+"_"+entity.getSiteId()+"_"+username, balance);
			this.redisTemplate.expire(entity.getLiveId()+"_"+entity.getSiteId()+"_"+username,60*60*24*7, TimeUnit.SECONDS);
		}
		return  JSONUtils.map2Json(resultMap);
	}

	@Override
	public String login(LoginParam param) {
		StringBuilder message = new StringBuilder();
		ApiInfoEntity entity = param.getEntity();
		String username = param.getUsername();
		username =MgConstants.MG_PREFIX + MgConstants.PARTNERID + entity.getPrefix() + username;
		
		message.append("MG login 站点：").append(entity.getSiteId());
		message.append(",会员：").append(username);
		
		String token=loginMember(username, entity);
		String launchUrl= loginGame(token, param.getGameType(),param.getAppId(), param.getBankingUrl(),param.getLobbyUrl(),param.getLogoutRedirectUrl(), param.getIsDemo(),param.getLanguage(),message);
		if(launchUrl==null){
			return JSONUtils.map2Json(failure("MG loginGame  is null"));
		}
		Map<String, Object> resultMap=success("MG login success");
		resultMap.put("launchUrl", launchUrl);
		return JSONUtils.map2Json(resultMap);
	}

	@Override
	public String loginBySingGame(LoginParam param) {
		return this.login(param);
	}

	@Override
	public MgApiUserEntity queryUserExist(String username) {
		MgApiUserEntityExample agApiUserExample = new MgApiUserEntityExample();
		agApiUserExample.createCriteria().andUsernameEqualTo(username);
		List<MgApiUserEntity> list = mgApiUserEntityMapper.selectByExample(agApiUserExample);
		return (list != null && list.size() > 0) ? list.get(0) : null;
	}

	@Override
	@Transactional
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		StringBuilder message = new StringBuilder();
		ApiInfoEntity entity = param.getEntity();
		String username = param.getUsername();
		
		message.append("MG checkAndCreateMember 站点：").append(entity.getSiteId());
		message.append(",会员：").append(username);
		
		//测试环境需要MG_PREFIX
		username =MgConstants.MG_PREFIX+MgConstants.PARTNERID+entity.getPrefix() + username; //DF91CSceshi
		MgApiUserEntity user = this.queryUserExist(username);
		if (user == null) {
			String token=loginWebSite(entity.getSiteId(),username);
			if(token==null){
				return failure("MG checkAndCreateMember token is null");	
			}
			
			mgMemberCreate(username,entity.getPassword(), token,entity.getCurrencyType(),entity.getLanguage(),message);

			MgApiUserEntity mgApiUserEntity=localMemberCreate(entity, username, entity.getPassword(),entity.getCurrencyType());
			if(mgApiUserEntity==null){
				return failure("MG checkAndCreateMember local memberCreate is fail");
			}
		}
		return success("MG user exist!");
	}

	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		return null;
	}

	
	//login to member api 
	private  String loginMember(String username,ApiInfoEntity entity){
		StringBuilder message = new StringBuilder();
		String ip=getRealIp();
		String passwd = entity.getPassword();
		logger.info("MG loginMember  username = {}, passwd = {}, ip = {}",username,passwd,ip);
		String token=null;
		message.append("MG loginMember 站点：").append(entity.getSiteId());
		message.append(",会员：").append(username);
		try{
			String xml = null;
			Document document;
			Element root;
			root = new Element("mbrapi-login-call");
			document = new Document(root);
			root.setAttribute("timestamp", DateUtil.format("yyyy-MM-dd HH:mm:ss", new Date())+" UTC");//此为测试，正式必须获取UTC时间
			root.setAttribute("apiusername", MgConstants.API_USERNAME);
			root.setAttribute("apipassword", MgConstants.API_PASSWD);
			root.setAttribute("username", username);
			root.setAttribute("password",passwd);
			root.setAttribute("ipaddress", ip);
			xml =MGUtil.doc2String(document);
			
			message.append("请求地址：").append(MgConstants.MG_MEMBER_URL).append("\n");
			message.append("请求参数：").append(xml).append("\n");
			HttpClient httpClients =  HttpClients.createDefault();
			HttpPost request = new HttpPost(MgConstants.MG_MEMBER_URL);
		    HttpResponse response=null;
		    request.addHeader("Content-Type",  ContentType.APPLICATION_XML.toString());
			StringEntity params =new StringEntity(xml);
			request.setEntity(params);
			logger.info("MG loginMember url={}, param={}",MgConstants.MG_MEMBER_URL,xml);
			response = httpClients.execute(request);
			String xmlResult=EntityUtils.toString(response.getEntity());
			message.append("接口返回：").append(xmlResult).append("\n");
			logger.info("MG loginMember  xmlResult={}",xmlResult);
			token = MGUtil.getXmlValue(xmlResult, "token");
		}catch(Exception e){
			message.insert("MG loginMember".length(),"获取token异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-mg loginMember",message.toString().replace("&", "*"));
			logger.error("MG loginMember error",e);
		}
		return token;
	}
	
	//Get Player Details & Balance 
	private String getPlayerBalance(String token,StringBuilder message){
		logger.info("MG getPlayerBalance  token = {}",token);
		String balance=null;
		try {
			Document document;
			Element root;
			root = new Element("mbrapi-account-call");
			document = new Document(root);
			String UtcTime = (String)TimeUtil.getUtcTime();
			root.setAttribute("timestamp", UtcTime+" UTC");//此为测试，正式必须获取UTC时间
			root.setAttribute("apiusername",MgConstants.API_USERNAME);
			root.setAttribute("apipassword",MgConstants.API_PASSWD);
			root.setAttribute("token", token);
			String xml = MGUtil.doc2String(document);
			message.append("请求地址：").append(MgConstants.MG_MEMBER_URL).append("\n");
			message.append("请求参数：").append(xml).append("\n");
			String result = MGUtil.sendXml(MgConstants.MG_MEMBER_URL, xml);
			message.append("接口返回：").append(result).append("\n");
			logger.info("MG getPlayerBalance  result={}",result);
			//创建一个新的字符串
	        StringReader read = new StringReader(result);
	        //创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
	        InputSource source = new InputSource(read);
	        //创建一个新的SAXBuilder
	        SAXBuilder sb = new SAXBuilder();
            //通过输入源构造一个Document
            Document doc = sb.build(source);
            //取的根元素
            root = doc.getRootElement();
            //得到根元素所有子元素的集合
            Element node = root.getChild("wallets");
            Element childNode =  node.getChild("account-wallet");
            String creditBalance = childNode.getAttributeValue("credit-balance");
            balance=creditBalance;
		}catch (Exception e) {
			logger.error("MG getPlayerBalance error",e);
			message.insert("MG getPlayerBalance".length(),"查询余额异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-mg getPlayerBalance",message.toString().replace("&", "*"));
		}
		return balance;
		
	}
	
	
	//login to API Website
	private  static String  loginWebSite(Integer siteId,String username){
		logger.info("MG loginWebSite  j_username = {} , j_password = {}",MgConstants.P_USM,MgConstants.P_PWD);
		StringBuilder message = new StringBuilder();
		message.append("MG loginWebSite 站点：").append(siteId);
		message.append(",会员：").append(username);
		String token=null;
		try {
		    HttpClient httpClients = HttpClients.createDefault();
			HttpPost request = new HttpPost(MgConstants.MG_WEBSITE_URL);
			request.addHeader("X-Requested-With", "X-Api-Client");
			request.addHeader("X-Api-Call", "X-Api-Client");
			Map<String,String>  param=new HashMap<String, String>();
			param.put("j_username", MgConstants.P_USM);
			param.put("j_password", MgConstants.P_PWD);
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : param.entrySet()) {
				formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
			request.setEntity(entity);
			message.append("请求地址：").append(MgConstants.MG_WEBSITE_URL).append("\n");
			message.append("请求参数：").append(param.toString()).append("\n");
		    HttpResponse response = httpClients.execute(request);
		    String content=EntityUtils.toString(response.getEntity());
		    message.append("接口返回：").append(content).append("\n");
			logger.info("MG loginWebSite  content={}",content);
		    JSONObject obj = JSONObject.parseObject(content);
		    token=obj.getString("token");
		} catch (Exception e) {
			logger.error("MG loginWebSite error",e);
			message.insert("MG loginWebSite".length(),"转账异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-mg loginWebSite",message.toString().replace("&", "*"));
		}
		return token;
	}
	
	
	//MG Member Creation
	private boolean mgMemberCreate(String username,String passwd,String token,String cur,String language,StringBuilder message){
		
		//测试环境代码
		if(username.contains(MgConstants.MG_PREFIX)){
			username=username.substring(username.indexOf(MgConstants.PARTNERID), username.length());
		}
		logger.info("MG mgMemberCreate  username = {} , passwd = {} , token = {}, cur = {}, language = {}" , username, passwd, token, cur, language);
		try {
		    HttpClient httpClients = HttpClients.createDefault();
		    HttpPut request = new HttpPut(MgConstants.MG_MEMCREATION_URL);
			request.addHeader("X-Requested-With", "X-Api-Client");
			request.addHeader("X-Api-Call", "X-Api-Client");
			request.addHeader("X-Api-Auth", token);  
			request.addHeader("Content-Type", "application/json");
			JSONObject casino=new JSONObject();
			casino.put("enable", true);
			JSONObject poker=new JSONObject();
			poker.put("enable", false);
			JSONObject  json=new JSONObject();
			json.put("crId", MgConstants.CRID);
			json.put("crType", MgConstants.CRTYPE);
			json.put("neId",MgConstants.NEID);
			json.put("neType",MgConstants.NETYPE);
			json.put("tarType",MgConstants.TARTYPE);
			json.put("username",username);
			json.put("name",username);
			json.put("password",passwd);
			json.put("confirmPassword",passwd);
			json.put("currency",cur);
			json.put("language",language);
			json.put("email","");
			json.put("casino",casino);
			json.put("poker", poker);
			logger.info("MG memberCreate url={}, params={}",MgConstants.MG_MEMCREATION_URL,json.toJSONString());
			StringEntity entity = new StringEntity(json.toJSONString());
			request.setEntity(entity);
			message.append("请求地址：").append(MgConstants.MG_MEMCREATION_URL).append("\n");
			message.append("请求参数：").append(json.toJSONString()).append("\n");
			HttpResponse response = httpClients.execute(request);
			String result =EntityUtils.toString(response.getEntity());
			message.append("接口返回：").append(result).append("\n");
			logger.info("MG mgMemberCreate  result={}",result);
			JSONObject obj=JSONObject.parseObject(result);
			if(obj.getBoolean("success")){
				return true;
			}else if(obj.getString("message").contains("Username existed")){
				return true;
			}
		} catch (Exception e) {
			message.insert("MG checkAndCreateMember".length(),"创建会员异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-mg checkAndCreateMember",message.toString().replace("&", "*"));
			logger.error("MG mgMemberCreate  error",e);
		}
		return false;
				
		
	} 
	
	//local Member Creation
	private MgApiUserEntity localMemberCreate(ApiInfoEntity entity,String username,String password,String cur){
		try {
			MgApiUserEntity mgApiUserEntity = new MgApiUserEntity();
			mgApiUserEntity.setUsername(username);
			mgApiUserEntity.setPassword(password);
			mgApiUserEntity.setApiInfoId(entity.getId().intValue());
			mgApiUserEntity.setAgentName(entity.getAgent()); 
			mgApiUserEntity.setUserStatus(1);
			mgApiUserEntity.setSiteId(entity.getSiteId());
			mgApiUserEntity.setSiteName(entity.getProjectAgent());
			mgApiUserEntity.setCurrencyType(cur);
			mgApiUserEntity.setCreateTime(com.ds.transfer.common.util.DateUtil.getCurrentTime());
			int insert = mgApiUserEntityMapper.insert(mgApiUserEntity); 
			logger.info("本地创建会员username = {}, result = {}", username, insert);
			return mgApiUserEntity;
		}catch(Exception e){
			logger.error("MG localMemberCreate error",e);
		}
		return null;
	}
	
	
	
	//Deposit  & Withdrawal 
	private boolean transaction(String token,String product,String operation,String amount,String txId,StringBuilder message){
		logger.info("MG transaction  token = {} , product = {} , operation = {}, amount = {}, txId = {}" ,  token, product, operation, amount, txId);
		try {
			Document document;
			Element root;
			root = new Element("mbrapi-changecredit-call");
			document = new Document(root);
			String UtcTime = (String)TimeUtil.getUtcTime();
			root.setAttribute("timestamp", UtcTime+" UTC");//此为测试，正式必须获取UTC时间
			root.setAttribute("apiusername",MgConstants.API_USERNAME);
			root.setAttribute("apipassword",MgConstants.API_PASSWD);
			root.setAttribute("token", token);
			root.setAttribute("product",product);
			root.setAttribute("operation",operation);
			root.setAttribute("amount",amount);
			root.setAttribute("tx-id",txId);
			String xml = MGUtil.doc2String(document);
			message.append("请求地址：").append(MgConstants.MG_MEMBER_URL).append("\n");
			message.append("请求参数：").append(xml).append("\n");
			String result = MGUtil.sendXml(MgConstants.MG_MEMBER_URL, xml);
			message.append("接口返回：").append(result);
			logger.info("MG transaction  result={}",result);
			//创建一个新的字符串
	        StringReader read = new StringReader(result);
	        //创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
	        InputSource source = new InputSource(read);
	        //创建一个新的SAXBuilder
	        SAXBuilder sb = new SAXBuilder();
            //通过输入源构造一个Document
            Document doc = sb.build(source);
            //取的根元素
            root = doc.getRootElement();
            if(MgConstants.MG_STATE_SUCCESS.equals(root.getAttributeValue("status"))){
            	return true;
            }
		}catch (Exception e) {
			message.insert("MG Transfer".length(),"转账异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-mg transfer",message.toString().replace("&", "*"));
			logger.error("MG transaction error",e);
		}
		return false;
	} 
	
	//Launch Game
		private String loginGame(String token,String gameId,String appId,String bankingUrl,String lobbyUrl,String logoutRedirectUrl,String demoMode,String language,StringBuilder message){
			logger.info("MG loginGame  token = {} , gameId = {} ,appId={}, bankingUrl = {}, lobbyUrl  = {}, logoutRedirectUrl = {},demoMode = {}, language = {}" ,  token, gameId,appId,bankingUrl, lobbyUrl, logoutRedirectUrl, demoMode, language );
			String launchUrl=null;
			try{
				Document document;
				Element root;
				root = new Element("mbrapi-launchurl-call");
				document = new Document(root);
				String UtcTime = (String)TimeUtil.getUtcTime();
				root.setAttribute("timestamp", UtcTime+" UTC");//此为测试，正式必须获取UTC时间
				root.setAttribute("apiusername",MgConstants.API_USERNAME);
				root.setAttribute("apipassword",MgConstants.API_PASSWD);
				root.setAttribute("token", token);
				root.setAttribute("language",language);
				root.setAttribute("gameId",gameId);
				root.setAttribute("app_id",appId);
				root.setAttribute("bankingUrl",bankingUrl);
				root.setAttribute("lobbyUrl",lobbyUrl);
				root.setAttribute("logoutRedirectUrl",logoutRedirectUrl);
				root.setAttribute("demoMode",demoMode=="0"?"true":"false");  //试玩根据MgConstants配置  该值为false
				String xml = MGUtil.doc2String(document);
				message.append("请求地址：").append(MgConstants.MG_MEMBER_URL).append("\n");
				message.append("请求参数：").append(xml).append("\n");
				logger.info("mg loginGame param={}",xml);
				String result = MGUtil.sendXml(MgConstants.MG_MEMBER_URL, xml);
				message.append("接口返回：").append(result).append("\n");
				logger.info("MG loginGame  result={}",result);
				//创建一个新的字符串
		        StringReader read = new StringReader(result);
		        //创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
		        InputSource source = new InputSource(read);
		        //创建一个新的SAXBuilder
		        SAXBuilder sb = new SAXBuilder();
	            //通过输入源构造一个Document
	            Document doc = sb.build(source);
	            //取的根元素
	            root = doc.getRootElement();
	            //得到根元素所有子元素的集合
	            String  status = root.getAttributeValue("status");
	            if(MgConstants.MG_STATE_SUCCESS.equals(status)){
	            	launchUrl=root.getAttributeValue("launchUrl");
	            }
			}catch(Exception e){
				message.insert("MG login".length(),"登陆异常！").append("\n");
				message.append("异常信息：").append(e.getMessage());
				telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-mg login",message.toString().replace("&", "*"));
				logger.error("MG loginGame error",e);
			}
			return launchUrl;
		}
		
		public static String getRealIp() {
			String localip = null;// 本地IP，如果没有配置外网IP则返回它
			String netip = null;// 外网IP
			Enumeration<NetworkInterface> netInterfaces;
			try {
				netInterfaces = NetworkInterface.getNetworkInterfaces();
		        InetAddress ip = null;
		        boolean finded = false;// 是否找到外网IP
		        while (netInterfaces.hasMoreElements() && !finded) {
		            NetworkInterface ni = netInterfaces.nextElement();
		            Enumeration<InetAddress> address = ni.getInetAddresses();
		            while (address.hasMoreElements()) {
		                ip = address.nextElement();
		                if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 外网IP
		                    netip = ip.getHostAddress();
		                    finded = true;
		                    break;
		                } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 内网IP
		                    localip = ip.getHostAddress();
		                }
		            }
		        }
		        if (netip != null && !"".equals(netip)) {
		            return netip;
		        } else {
		            return localip;
		        }
			} catch (Exception e) {
		       logger.info("get ip error",e);
			}
			return null;
		}

		@Override
		public String queryAgentBalance(QueryBalanceParam param) {
			Map<String, Object> resultMap = null;
			double balance = 0;  
			try {
				String agent = param.getAgent();
				String siteId = String.valueOf(param.getEntity().getSiteId());
				logger.info("mg 查询redis缓存代理总额度 agent{}, siteId{}",agent,siteId);
				Set<String> keys = this.redisTemplate.keys(param.getEntity().getLiveId()+"_"+siteId+"_*");
				Iterator<String> it = keys.iterator();  
		        while(it.hasNext()){  
		            String siteUser = it.next();  
		            String balanceStr = this.redisTemplate.opsForValue().get(siteUser);
		            if(!StringsUtil.isNull(balanceStr)){
		            	balance = BigDemicalUtil.add(balance,Double.valueOf(balanceStr),2);
		            }
		        }
				resultMap = success(String.valueOf(balance));
				resultMap.put("balance", String.valueOf(balance));
				return JSONUtils.map2Json(resultMap);
			} catch (Exception e) {
				logger.error("查询代理余额异常 : ", e);
				resultMap = maybe("查询余额异常");
			}
			return JSONUtils.map2Json(resultMap);
		}
}
