package com.ds.transfer.pt.service;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.ds.msg.TelegramMessage;
import com.ds.transfer.common.constants.SysConstants;
import com.ds.transfer.common.service.CommonTransferService;
import com.ds.transfer.common.service.TransferService;
import com.ds.transfer.common.util.BigDemicalUtil;
import com.ds.transfer.common.util.DateUtil;
import com.ds.transfer.common.util.JSONUtils;
import com.ds.transfer.common.util.StringsUtil;
import com.ds.transfer.common.vo.LoginParam;
import com.ds.transfer.common.vo.QueryBalanceParam;
import com.ds.transfer.common.vo.QueryOrderStatusParam;
import com.ds.transfer.common.vo.TransferParam;
import com.ds.transfer.common.vo.UserParam;
import com.ds.transfer.pt.constants.PtConstants;
import com.ds.transfer.pt.constants.TelegramConstants;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.PtApiUserEntity;
import com.ds.transfer.record.entity.PtApiUserEntityExample;
import com.ds.transfer.record.entity.TransferRecordEntity;
import com.ds.transfer.record.mapper.PtApiUserEntityMapper;
import com.ds.transfer.record.service.TransferRecordService;
/**
* @ClassName: TransferServiceImpl 
* @Description: TODO(转账业务) 
* @author leo
* @date 2017年10月23日 上午11:10:16  
* @Copyright: 2017 鼎泰科技 Inc. All rights reserved. 
* 注意：本内容仅限于鼎泰科技有限公司内部传阅，禁止外泄以及用于其他的商业目
 */
@Service
public class TransferServiceImpl extends CommonTransferService implements TransferService<PtApiUserEntity> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource(name = "moneyCenterService")
	private TransferService<?> moneyCenterService;

	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;

	@Resource
	private PtApiUserEntityMapper ptApiUserEntityMapper;
	
	@Autowired
	protected RedisTemplate<String, String> redisTemplate;
	
	//小飞机消息组件
	TelegramMessage telegramMessage = TelegramMessage.getInstance();
	
	@Override
	public String transfer(TransferParam transferParam) {
		TransferRecordEntity ptRecord = new TransferRecordEntity();
		StringBuilder message = new StringBuilder();
		Map<String,Object> sendParam = new HashMap<String,Object>();
		String typeDes = null;
		String result = "";
		String msg = "";
		try {
			String username = transferParam.getUsername();
			ApiInfoEntity entity = transferParam.getEntity();
			String platformUser = (entity.getPrefix() +"_"+ username).toUpperCase();
			String type = transferParam.getType();
			String memoy = transferParam.getCredit();
			String billno = transferParam.getBillno();
			typeDes = IN.equals(type) ? "入" : "出";
			
			message.append("PT Transfer 站点：").append(entity.getSiteId());
			message.append(",会员：").append(transferParam.getUsername());
			message.append(",转账：").append(transferParam.getType().equals("IN")==true?"DS-->PT":"PT-->DS");
			message.append(",单号:").append(transferParam.getBillno());
			message.append(",金额：").append(transferParam.getCredit()).append("\n");
			
			//1.插入记录
			ptRecord = this.transferRecordService.insert(entity.getSiteId(), entity.getLiveId(), transferParam.getTransRecordId(),
					entity.getPassword(), transferParam.getUsername(),memoy, billno,type, PtConstants.PT,transferParam.getRemark(), ptRecord);
			logger.info("PT电子 转账记录插入成功,  id = {}", ptRecord.getId());
			
			//2.拼接url 参数
			StringBuffer urlParam = new StringBuffer();
			urlParam.append("/playername/").append(platformUser);
			urlParam.append("/amount/").append(memoy);
			urlParam.append("/adminname/").append(entity.getAgent());
			urlParam.append("/externaltranid/").append(billno);
			
			//3.组装请求安全协议密钥类参数
			PropertyConfigurator.configure("resource" + File.separator + "pt_play.p12");
			sendParam.put("certificate", "resource" + File.separator + "pt_play.p12");
			sendParam.put("crt_pwd",PtConstants.CRT_PWD);
			sendParam.put("pt_url",entity.getReportUrl()+ (IN.equals(type) == true ? PtConstants.TRANSFER_IN:PtConstants.TRANSFER_OUT+"/isForce/1")+urlParam.toString());
			sendParam.put("entity_key",entity.getPtEntityKey());
			message.append("请求报文：").append(sendParam).append("\n");
			
			//4.发送远程请求
			String response = StringsUtil.sendGetPT(sendParam);
			message.append("接口返回：").append(response).append("\n");
			JSONObject jsonMap = JSONObject.parseObject(response);
			
			if (jsonMap.containsKey("result")) {
				logger.info("PT电子 转账成功,type = {}", type);
				//更新转账信息
				ptRecord = this.transferRecordService.update(SysConstants.Record.TRANS_SUCCESS, "PT转账成功", ptRecord);
				return JSONUtils.map2Json(success("PT 转" + typeDes + "成功"));
			}else{
				ptRecord = this.transferRecordService.update(SysConstants.Record.TRANS_FAILURE, "PT转账失败", ptRecord);
				if (IN.equals(type)) {
					logger.info("PT 转入失败,退回DS主账户");
					transferParam.setRemark("PT 转入失败,退回DS主账户");
					transferParam.setBillno(billno+"F");
					result = this.moneyCenterService.transfer(transferParam);
					logger.info("PT 转入失败,退回DS主账户调用主钱包result = {}",result);
					Map<String, Object> resultMap = JSONUtils.json2Map(result);
					if (SUCCESS.equals(resultMap.get(STATUS))) {
						return JSONUtils.map2Json(failure(resultMap, "PT 转入失败:" + msg + ",退回DS主账户"));
					}
				}
				ptRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "PT转账异常", ptRecord);
				message.insert("PT Transfer".length(),"转账异常！").append("\n");
				telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-pt transfer",message.toString().replace("&", "*"));
			}
		} catch (Exception e) {
			logger.error("PT电子  转" + typeDes + "异常 : ", e);
			ptRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "PT转账异常", ptRecord);
			message.insert("PT Transfer".length(),"转账异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-pt transfer",message.toString().replace("&", "*"));
			return JSONUtils.map2Json(maybe("PT电子  转" + typeDes + "异常"));
		}
		return JSONUtils.map2Json(failure("PT电子  转" + typeDes + "失败:" + msg));
	}
	

	@Override
	public String queryBalance(QueryBalanceParam param) {
		StringBuilder message = new StringBuilder();
		
		Map<String, Object> resultMap = null;
		Map<String,Object> sendParam = new HashMap<String,Object>();
		
		try {
			ApiInfoEntity entity = param.getEntity();
			String platformUser = entity.getPrefix().concat("_").concat(param.getUsername());
			String username = param.getUsername();
			
			message.append("PT qeuryBalance 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			//1.URL参数拼接
			StringBuffer urlParam = new StringBuffer();
			urlParam.append("/playername/").append(platformUser.toUpperCase());
			
			//2.组装请求安全协议密钥类参数
			PropertyConfigurator.configure("resource" + File.separator + "pt_play.p12");
			sendParam.put("certificate", "resource" + File.separator + "pt_play.p12");
			sendParam.put("crt_pwd",PtConstants.CRT_PWD);
			sendParam.put("pt_url",entity.getReportUrl()+ PtConstants.QUERY_BALANCE + urlParam.toString());
			sendParam.put("entity_key",entity.getPtEntityKey());
			logger.info("PT电子  余额查询 ,会员:{},代理:{}",param.getUsername(),entity.getAgent());
			message.append("请求报文：").append(sendParam).append("\n");
			
			//3.发起远程请求
			String result = StringsUtil.sendGetPT(sendParam);
			message.append("接口返回：").append(result).append("\n");
			
			JSONObject json = JSONObject.parseObject(result);
			if(json.containsKey("result")){
				JSONObject jsonObj = (JSONObject)json.get("result");
				String balance = jsonObj.getString("balance");
				resultMap = success("success!");
				resultMap.put("balance", balance);
				//正则判断值是否标准金额
				if(BigDemicalUtil.checkoutMoney(balance)){
					//查询到的余额添加到缓存，用于批量查询,缓存7天自动清除
					this.redisTemplate.opsForValue().set(entity.getLiveId()+"_"+entity.getSiteId()+"_"+username, balance);
					this.redisTemplate.expire(entity.getLiveId()+"_"+entity.getSiteId()+"_"+username,60*24*7, TimeUnit.MINUTES);
				}
				return JSONUtils.map2Json(resultMap);
			}
			resultMap = failure(JSONObject.toJSONString(json));
			message.insert("PT qeuryBalance".length(),"查询余额异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-pt queryBalance",message.toString().replace("&","*"));
		} catch (Exception e) {
			logger.error("PT电子查询余额异常 : ", e);
			resultMap = maybe("PT电子 查询余额异常");
			
			message.insert("PT qeuryBalance".length(),"查询余额异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-pt queryBalance",message.toString().replace("&","*"));
		}
		return JSONUtils.map2Json(resultMap);
	}
	
	@Override
	public String queryAgentBalance(QueryBalanceParam param) {
		Map<String, Object> resultMap = null;
		double balance = 0;  
		try {
			String agent = param.getAgent();
			String siteId = String.valueOf(param.getEntity().getSiteId());
			logger.info("pt 查询redis缓存代理总额度 agent{}, siteId{}",agent,siteId);
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

	
	@Override
	public String login(LoginParam param) {
		Map<String,Object> resultMessage = new HashMap<String,Object>();
		Map<String,Object> resultMap = new HashMap<String,Object>();
		try {
			resultMap.put("username",param.getUsername());
			resultMap.put("password",param.getPassWord());
			resultMessage.put("message", resultMap);
			resultMessage.put("status", "10000");
			logger.info("pt login result = {}",JSONUtils.map2Json(resultMessage));
		} catch (Exception e) {
			logger.info("登陆异常 : ", e);
			return JSONUtils.map2Json(maybe("登录PT电子游戏内部出现异常!"));
		}
		return JSONUtils.map2Json(resultMessage);
	}


	@Override
	@Transactional
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		Map<String, Object> resultMap = new HashMap<String,Object>();
		String platformUser = "";
		try {
			String cur = StringsUtil.isNull(param.getCur()) ? SysConstants.CUR : param.getCur();
			String oddType = StringsUtil.isNull(param.getOddtype()) ? SysConstants.CUR : param.getOddtype();
			ApiInfoEntity entity = param.getEntity();
			String prefix = entity.getPrefix();
			platformUser = (prefix.concat("_").concat(param.getUsername())).toUpperCase();
			String username = (entity.getAgent() +"|"+ platformUser).toUpperCase();
			entity.setPassword(StringsUtil.randomString(12));				//生成12位随机密码
			
			//1.查询本地数据库是否存在该用户
			PtApiUserEntity user = this.queryUserExist(username);
			
			if (user == null) {
				//2.创建pt会员
				resultMap = this.createMemberByPt(param, entity,platformUser);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					//3.创建本地会员
					user = this.createMemberByLocal(param, entity, platformUser, cur, oddType);
					resultMap.put("userInfo", user);
					return success(resultMap,"创建pt会员"+platformUser+"成功");
				}
			}
			logger.info("用户{},本地已经存在",platformUser);
			resultMap.put("userInfo",user);
			return success(resultMap,"PT会员"+platformUser+"已经存在");
		} catch (Exception e) {
			logger.error("创建pt会员{},异常 : ",platformUser,e);
			return maybe("创建pt会员"+platformUser+"异常");
		}
	}
	

	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		StringBuilder message = new StringBuilder();
		Map<String,Object> sendParam = new HashMap<String,Object>();
		String result = null;
		try {
			String billno = param.getBillno();
			ApiInfoEntity entity = param.getEntity();
			message.append("PT queryStatusByBillno 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			//1.组装请求安全协议密钥类参数
			PropertyConfigurator.configure("resource" + File.separator + "pt_play.p12");
			sendParam.put("certificate", "resource" + File.separator + "pt_play.p12");
			sendParam.put("crt_pwd",PtConstants.CRT_PWD);
			sendParam.put("pt_url",entity.getReportUrl()+PtConstants.CHECK + "/externaltransactionid/"+billno);
			sendParam.put("entity_key",entity.getPtEntityKey());
			logger.info("pt queryStatusByBillno request param={}",sendParam.toString());
			message.append("请求报文：").append(sendParam).append("\n");
			//2.远程调用
			String response = StringsUtil.sendGetPT(sendParam);
			message.append("接口返回：").append(response).append("\n");
			logger.info("pt queryStatusByBillno billno={},username={}, result ",billno,param.getUsername(),response);
			JSONObject jsonMap = JSONObject.parseObject(response);
			result = JSONObject.toJSONString(jsonMap);
			String status = jsonMap.getJSONObject("result").getString("status");
			if ("approved".equals(status)) {
				return JSONUtils.map2Json(success(result));
			} else {
				message.insert("PT queryStatusByBillno".length(),"查询订单状态未知！").append("\n");
				telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-pt queryStatusByBillno",message.toString().replace("&","*"));
				logger.info("未知状态 = {}", status);
			}
		} catch (Exception e) {
			logger.error("查询订单状态错误 : ", e);
			message.insert("PT queryStatusByBillno".length(),"查询订单状态未知！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-pt queryStatusByBillno",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("查询订单状态出错 : " + result));
		}
		return JSONUtils.map2Json(failure(result));
	}

	/**
	 * 本地创建会员
	 */
	private PtApiUserEntity createMemberByLocal(UserParam param, ApiInfoEntity entity, String username, String cur, String oddType) {
		PtApiUserEntity ptUser = new PtApiUserEntity();
		ptUser.setUsername(username);
		ptUser.setPassword(entity.getPassword());
		ptUser.setAgentName(entity.getAgent());
		ptUser.setApiInfoId(entity.getId().intValue());//api 的 id
		ptUser.setSiteName(entity.getProjectAgent());
		ptUser.setCurrencyType(cur);
		ptUser.setOddtype(oddType);
		ptUser.setCreateTime(DateUtil.getCurrentTime());//创建时间
		ptUser.setUserStatus(1);//状态=1 有效会员
		ptUser.setSiteId(entity.getSiteId());
		ptUser.setSiteName(entity.getProjectAgent());
		this.ptApiUserEntityMapper.insert(ptUser);
		return ptUser;
	}
	
	/**
	* @Title: createMemberByBbin 
	* @Package com.ds.transfer.pt.service
	* @Description: TODO(PT创建会员) 
	* @param @param param
	* @param @param entity
	* @param @param username
	* @param @return
	* @param @throws Exception    设定文件 
	* @return Map<String,Object>    返回类型 
	* @date: 2017年10月25日 上午10:41:23  
	* @author: leo 
	* @version V1.0
	* @Copyright: 2017 鼎泰科技 Inc. All rights reserved. 
	* 注意：本内容仅限于鼎泰科技有限公司内部传阅，禁止外泄以及用于其他的商业目
	*/
	private Map<String, Object> createMemberByPt(UserParam userParam, ApiInfoEntity entity, String username) throws Exception {
		StringBuilder message = new StringBuilder();
		message.append("PT createMember 站点：").append(entity.getSiteId());
		message.append(",会员：").append(username);
		//1.接口参数组装
		StringBuffer urlParam = new StringBuffer();
		urlParam.append("/playername/").append(username);
		urlParam.append("/adminname/").append(entity.getAgent());
		urlParam.append("/kioskname/").append(entity.getPrefix());
		urlParam.append("/custom02/").append(PtConstants.CUSTOM02);
		urlParam.append("/password/").append(entity.getPassword());
		
		//2.组装请求安全协议密钥类参数
		Map<String,Object> sendParam = new HashMap<String,Object> ();
		sendParam.put("certificate", "resource" + File.separator + "pt_play.p12");
		sendParam.put("crt_pwd",PtConstants.CRT_PWD);
		sendParam.put("pt_url",entity.getReportUrl()+PtConstants.CREATE_MEMBER + urlParam.toString());
		sendParam.put("entity_key",entity.getPtEntityKey());
		message.append("请求报文：").append(sendParam).append("\n");
		
		//3.远程调用PT创建用户
		String result = StringsUtil.sendGetPT(sendParam);
		message.append("接口返回：").append(result).append("\n");
		JSONObject responseJson = JSONObject.parseObject(result);
		if(responseJson.containsKey("result")){
			logger.info("pt创建会员{},成功!", username);
			return success("pt创建会员:"+username+",成功!" );
		}else{
			if(19 == responseJson.getIntValue("errorcode")){
				StringBuffer replacePwdParam = new StringBuffer();
				replacePwdParam.append("/playername/").append(username);
				replacePwdParam.append("/password/").append(entity.getPassword());
				sendParam.put("pt_url", entity.getReportUrl()+PtConstants.REPLACE_PASSWORD+replacePwdParam.toString());
				message.append("请求报文：").append(sendParam).append("\n");
				result = StringsUtil.sendGetPT(sendParam);
				message.append("接口返回：").append(result).append("\n");
				logger.info("pt创建会员{},已经存在,并修改密码成功 ", username);
				return success("pt会员:"+username+",已经存在 !");
			}
		}
		message.insert("PT createMember".length(),"创建会员异常！").append("\n");
		telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-pt createMember",message.toString().replace("&","*"));
		return failure(JSONObject.toJSONString(responseJson));
	}

	@Override
	public PtApiUserEntity queryUserExist(String usernames) {
		String[] username = usernames.split("\\|");
		PtApiUserEntityExample ptApiUserExample = new PtApiUserEntityExample();
		ptApiUserExample.createCriteria().andUsernameEqualTo(username[1]).andAgentNameEqualTo(username[0]);
		List<PtApiUserEntity> list = this.ptApiUserEntityMapper.selectByExample(ptApiUserExample);
		return (list == null || list.size() == 0) ? null : list.get(0);
	}
	
	/**
	 * 进入单个游戏
	 */
	@SuppressWarnings("unused")
	private String playGame(ApiInfoEntity entity, String username, String password, String gamekind, String gametype, String gamecode, String lang) {
		return null;
	}
	
	@Override
	public String loginBySingGame(LoginParam param) {
		return null;
	}
}

