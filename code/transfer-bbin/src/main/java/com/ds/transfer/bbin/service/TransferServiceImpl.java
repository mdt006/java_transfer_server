package com.ds.transfer.bbin.service;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.ds.msg.TelegramMessage;
import com.ds.transfer.bbin.constants.BbinConstants;
import com.ds.transfer.bbin.constants.TelegramConstants;
import com.ds.transfer.bbin.vo.BBinTransferVo;
import com.ds.transfer.common.constants.SysConstants;
import com.ds.transfer.common.service.CommonTransferService;
import com.ds.transfer.common.service.TransferService;
import com.ds.transfer.common.util.BigDemicalUtil;
import com.ds.transfer.common.util.DateUtil;
import com.ds.transfer.common.util.JSONUtils;
import com.ds.transfer.common.util.ReflectUtil;
import com.ds.transfer.common.util.StringsUtil;
import com.ds.transfer.common.vo.LoginParam;
import com.ds.transfer.common.vo.QueryBalanceParam;
import com.ds.transfer.common.vo.QueryOrderStatusParam;
import com.ds.transfer.common.vo.TransferParam;
import com.ds.transfer.common.vo.UserParam;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.BbinApiUserEntity;
import com.ds.transfer.record.entity.BbinApiUserEntityExample;
import com.ds.transfer.record.entity.TransferRecordEntity;
import com.ds.transfer.record.mapper.BbinApiUserEntityMapper;
import com.ds.transfer.record.service.TransferRecordService;

/**
 * 转账业务
 * @author jackson
 */
@Service
public class TransferServiceImpl extends CommonTransferService implements TransferService<BbinApiUserEntity> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource(name = "moneyCenterService")
	private TransferService<?> moneyCenterService;

	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;

	@Resource
	private BbinApiUserEntityMapper bbinApiUserEntityMapper;
	
	@Autowired
	protected RedisTemplate<String, String> redisTemplate;
	
	//小飞机消息组件
	TelegramMessage telegramMessage = TelegramMessage.getInstance();

	@Override
	public String transfer(TransferParam transferParam) {
		TransferRecordEntity bbinRecord = new TransferRecordEntity();
		StringBuilder message = new StringBuilder();
		String typeDes = null;
		String msg = "";
		try {
			ApiInfoEntity entity = transferParam.getEntity();
			String platformUser = entity.getPrefix().concat(transferParam.getUsername());
			String type = transferParam.getType();
			typeDes = IN.equals(type) ? "入" : "出";
			
			message.append("BBIN Transfer 站点：").append(entity.getSiteId());
			message.append("会员：").append(transferParam.getUsername());
			message.append(",转账：").append(transferParam.getType().equals("IN")==true ? "DS-->BBIN":"BBIN-->DS");
			message.append(",单号:").append(transferParam.getBillno());
			message.append(",金额：").append(transferParam.getCredit()).append("\n");
			
			//1.插入记录
			bbinRecord = this.transferRecordService.insert(entity.getSiteId(), entity.getLiveId(), transferParam.getTransRecordId(), entity.getPassword(), transferParam.getUsername(), transferParam.getCredit(), transferParam.getBillno(),//
					type, BbinConstants.BBIN, transferParam.getRemark(), bbinRecord);
			logger.info("BBIN视讯 转账记录插入成功,  id = {}", bbinRecord.getId());

			//2.转账
			String bbinKey = bbinKey(BbinConstants.WEB_SITE, platformUser, transferParam.getBillno(), BbinConstants.KEYB);
			BBinTransferVo vo = new BBinTransferVo(BbinConstants.WEB_SITE, platformUser, entity.getAgent(), transferParam.getBillno(), type, Integer.valueOf(transferParam.getCredit()), bbinKey);
			String param = ReflectUtil.generateParam(vo);
			message.append(" 转账地址：").append(entity.getReportUrl()+BbinConstants.function.TRANSFER).append("\n");
			message.append(" 转账参数：").append(param).append("\n");
			String result = StringsUtil.sendPost1(entity.getReportUrl() + BbinConstants.function.TRANSFER, param);
			message.append(" 接口返回：").append(result).append("\n");
			logger.info("bbin transfer siteId={},username={},result={}",entity.getSiteId(),transferParam.getUsername(),result);
			JSONObject jsonMap = JSONObject.parseObject(result);
			msg = jsonMap.getJSONObject("data").getString("Message");
			if (jsonMap.getBooleanValue("result")) {
				if (BbinConstants.STATE_SUCCESS.equals(jsonMap.getJSONObject("data").get("Code"))) {
					logger.info("bbin 转账成功,type = {}", type);
					//更新转账信息
					bbinRecord = this.transferRecordService.update(SysConstants.Record.TRANS_SUCCESS, "BBIN转账成功", bbinRecord);
					return JSONUtils.map2Json(success("BBIN 转" + typeDes + "成功"));
				}
			}
			bbinRecord = this.transferRecordService.update(SysConstants.Record.TRANS_FAILURE, "BBIN转账失败", bbinRecord);
			
			message.insert("BBIN Transfer".length(),"转账异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-bbin transfer",message.toString().replace("&", "*"));
			
			if (IN.equals(type)) {
				logger.info("BBIN 转入失败,退回DS主账户");
				transferParam.setRemark("BBIN 转入失败,退回DS主账户");
				transferParam.setBillno(transferParam.getBillno() + "F");
				result = this.moneyCenterService.transfer(transferParam);
				Map<String, Object> resultMap = JSONUtils.json2Map(result);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					return JSONUtils.map2Json(failure(resultMap, "BBIN 转入失败:" + msg + ",退回DS主账户"));
				}
				logger.info("bbin transfer siteId={},username={},billno={}转入bbin失败,退回DS调用钱包失败!",entity.getSiteId(),
						transferParam.getUsername(),transferParam.getBillno());
			}
		} catch (Exception e) {
			logger.error("bbin 转" + typeDes + "异常 : ", e);
			bbinRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "BBIN转账异常", bbinRecord);
			message.insert("BBIN Transfer".length(),"转账异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-bbin transfer",message.toString().replace("&", "*"));
			return JSONUtils.map2Json(maybe("BBIN 转" + typeDes + "异常"));
		}
		return JSONUtils.map2Json(failure("BBIN 转" + typeDes + "失败:" + msg));
	}

	@Override
	public String queryBalance(QueryBalanceParam param) {
		StringBuilder message = new StringBuilder();
		Map<String, Object> resultMap = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			String platformUser = entity.getPrefix().concat(param.getUsername());
			
			message.append("BBIN qeuryBalance 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername()).append("\n");
			
			BbinApiUserEntity user = this.queryUserExist(platformUser);
			logger.info("查询本地是否存在此会员{}-->{},user={}",platformUser,user !=null?"已存在":"不存在走远程创建",user);
			if(user == null){
				LoginParam loginParam = new LoginParam(entity, platformUser);
				loginParam.setPageSite(null);
				loginParam.setLanguage(BbinConstants.LANG);
				//远程创建BBIN用户
				logger.info("bbin method queryBalance 远程创建会员 ：{}",platformUser);
				this.login(loginParam);
			}
			//查询余额
			String key = this.bbinKeyGenerate(BbinConstants.WEB_SITE, platformUser, BbinConstants.BALANCE_KEY, null, null);
			logger.debug("{}, {}, {}, {}", BbinConstants.WEB_SITE, platformUser, entity.getAgent(), key);
			BBinTransferVo vo = new BBinTransferVo(BbinConstants.WEB_SITE, platformUser, entity.getAgent(), key);
			String result = ReflectUtil.generateParam(vo);
			message.append("请求地址：").append(BbinConstants.BBIN_URL + BbinConstants.function.QUERY_BALANCE).append("\n");
			message.append("请求参数：").append(result).append("\n");
			
			long startTime = System.currentTimeMillis();
			result = StringsUtil.sendPost1(BbinConstants.BBIN_URL + BbinConstants.function.QUERY_BALANCE, result);
			long endTime = System.currentTimeMillis();
			message.append("接口返回：").append(result);
			
			logger.info("bbin remote query balance site_id={},username={},耗时:[{}]毫秒 ,result={}",entity.getSiteId(),platformUser,(endTime-startTime),result);
			JSONObject json = JSONObject.parseObject(result);
			if (json.getBooleanValue("result")) {
				String balance = json.getJSONArray("data").getJSONObject(0).getString("Balance");
				resultMap = success(balance);
				resultMap.put("balance", balance);
				//正则判断值是否标准金额
				if(BigDemicalUtil.checkoutMoney(balance)){
					//查询到的余额添加到缓存，用于批量查询,缓存7天自动清除
					this.redisTemplate.opsForValue().set(entity.getLiveId()+"_"+entity.getSiteId()+"_"+platformUser, balance);
					this.redisTemplate.expire(entity.getLiveId()+"_"+entity.getSiteId()+"_"+platformUser,60*24*7, TimeUnit.MINUTES);
				}
				return JSONUtils.map2Json(resultMap);
			}
			resultMap = failure(json.getJSONObject("data").getString("Message"));
			
			message.insert("BBIN qeuryBalance".length(),"查询余额异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-bbin qeuryBalance",message.toString().replace("&","*"));
		} catch (Exception e) {
			logger.error("查询余额异常 : ", e);
			resultMap = maybe("查询余额异常");
			message.insert("BBIN qeuryBalance".length(),"查询余额异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-bbin qeuryBalance",message.toString().replace("&","*"));
		}
		return JSONUtils.map2Json(resultMap);
	}
	
	@Override
	public String queryAgentBalance(QueryBalanceParam param) {
		Map<String, Object> resultMap = null;
		double balance = 0;  
		try {
			ApiInfoEntity entity = param.getEntity();
			String agent = param.getAgent();
			String siteId = String.valueOf(entity.getSiteId());
			logger.info("bbin 查询redis缓存代理总额度 agent{}, siteId{}",agent,siteId);
			Set<String> keys = this.redisTemplate.keys(entity.getLiveId()+"_"+siteId+"_*");
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
		StringBuilder message = new StringBuilder();
		String result = null;
		
		try {
			ApiInfoEntity entity = param.getEntity();
			String username = param.getUsername();
			
			message.append("BBIN login 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			String key = this.bbinKeyGenerate(BbinConstants.WEB_SITE, username, BbinConstants.LOGIN_KEY, 7, 5);
			BBinTransferVo vo = new BBinTransferVo(BbinConstants.WEB_SITE, username, entity.getAgent(), entity.getPassword(), param.getLanguage(), param.getPageSite(), key);
			result = ReflectUtil.generateParam(vo);
			if("live".equals(param.getPageSite())){
				result = result.concat("&page_present=live");
			}
			String loginUrl = StringsUtil.isNull(param.getLoginUrl()) != true ? param.getLoginUrl() : entity.getBbinUrl() + BbinConstants.function.LOGIN;
			message.append("请求地址：").append(loginUrl).append("\n");
			message.append("请求参数：").append(result).append("\n");
			result = StringsUtil.sendPost1(loginUrl, result);
			message.append("接口返回：").append(result).append("\n");
			logger.info("siteId={},username：{},login bbin result:{}",entity.getSiteId(),username,result);
			
			if (result.contains("网站维护通知") || result.contains("System is in maintenance")
					|| result.contains("SQL Error") || StringsUtil.isNull(result) || "ErrorCode".contains(result)) {
				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(STATUS, MAINTAIN);
				resultMap.put(MESSAGE, result);
				return JSONUtils.map2Json(resultMap);
			}
		} catch (Exception e) {
			logger.error("登录异常", e);
			message.insert("BBIN login".length(),"登录异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-bbin login",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("登录bbin内部出现异常!"));
		}
		return JSONUtils.map2Json(success(result,"0000"));
	}

	@Override
	public String loginBySingGame(LoginParam param) {
		StringBuilder message = new StringBuilder();
		String result = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			String platformUser = entity.getPrefix().concat(param.getUsername());
			
			message.append("BBIN loginBySingGame 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			String key = this.bbinKeyGenerate(BbinConstants.WEB_SITE, platformUser, BbinConstants.LOGIN_KEY, 7, 5);
			BBinTransferVo vo = new BBinTransferVo(BbinConstants.WEB_SITE, platformUser, entity.getAgent(), entity.getPassword(), param.getLanguage(), key);
			String isFlash = param.getIsFlash();
			String loginParam = ReflectUtil.generateParam(vo);
			String login2Url = StringsUtil.isNull(param.getLoginUrl()) != true ? param.getLoginUrl()+BbinConstants.function.LOGIN_SINGLE : entity.getBbinUrl() + BbinConstants.function.LOGIN_SINGLE;
			message.append("请求地址：").append(login2Url).append("\n");
			message.append("请求参数：").append(loginParam).append("\n");
			result = StringsUtil.sendPost1(login2Url, loginParam);
			message.append("接口返回：").append(result).append("\n");
			
			if (result.contains("网站维护通知") || result.contains("System is in maintenance")) {
				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(STATUS, MAINTAIN);
				resultMap.put(MESSAGE, result);
				return JSONUtils.map2Json(resultMap);
			}
			if (result.contains("<meta http-equiv='Content-Type'")) {//返回错误
				return JSONUtils.map2Json(failure(result.substring(result.indexOf("("), result.indexOf(")") + 1)));
			}
			JSONObject jsonMap = JSONObject.parseObject(result);
			logger.info("result = {}, code = ", jsonMap.getBooleanValue("result"), jsonMap.getJSONObject("data").getString("Code"));
			if (jsonMap.getBooleanValue("result")) {
				if ("99999".equals(jsonMap.getJSONObject("data").getString("Code"))) {
					result = this.playGame(entity, platformUser, entity.getPassword(), param.getGamekind(), param.getGameType(), param.getGamecode(), param.getLanguage(),param.getLoginUrl(),isFlash);
					return JSONUtils.map2Json(success(result));
				}
			}
		} catch (Exception e) {
			logger.info("登陆异常 : ", e);
			message.insert("BBIN loginBySingGame".length(),"登录异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-bbin loginBySingGame",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("登录bbin电子游戏内部出现异常!"));
		}
		return JSONUtils.map2Json(failure(result));
	}

	@Override
	public BbinApiUserEntity queryUserExist(String username) {
		BbinApiUserEntityExample bbinApiUserExample = new BbinApiUserEntityExample();
		bbinApiUserExample.createCriteria().andUsernameEqualTo(username);
		List<BbinApiUserEntity> list = this.bbinApiUserEntityMapper.selectByExample(bbinApiUserExample);
		return (list == null || list.size() == 0) ? null : list.get(0);
	}

	@Override
	@Transactional
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			String cur = StringsUtil.isNull(param.getCur()) ? SysConstants.CUR : param.getCur();
			String oddType = StringsUtil.isNull(param.getOddtype()) ? SysConstants.CUR : param.getOddtype();
			ApiInfoEntity entity = param.getEntity();
			String platformUser = entity.getPrefix().concat(param.getUsername());
			BbinApiUserEntity user = this.queryUserExist(platformUser);
			if (user == null) {
				//1.创建bbin会员
				LoginParam loginParam = new LoginParam(entity,platformUser);
				logger.info("bbin method checkAndCreateMember 远程创建用户：{}",platformUser);
				resultMap = JSONUtils.json2Map(this.login(loginParam));
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					//2.创建本地会员
					user = this.createMemberByLocal(param, entity, platformUser, cur, oddType);
				}
				return resultMap;
			}
		} catch (Exception e) {
			logger.error("创建bbin会员异常 : ", e);
			return maybe("创建bbin会员异常");
		}
		return success("用户已经存在");
	}

	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		StringBuilder message = new StringBuilder();
		String result = null;
		try {
			String billno = param.getBillno();
			ApiInfoEntity entity = param.getEntity();
			
			message.append("BBIN queryStatusByBillno 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			BBinTransferVo vo = new BBinTransferVo(BbinConstants.WEB_SITE, billno, queryOrderStatusKeyGenerate(BbinConstants.WEB_SITE, BbinConstants.CHECK_TRANSFER));
			result = ReflectUtil.generateParam(vo);
			
			message.append("请求地址：").append(entity.getReportUrl()).append("\n");
			message.append("请求参数：").append(result).append("\n");
			result = StringsUtil.sendPost1(entity.getReportUrl() + BbinConstants.function.CHECK_TRANSFER, result);
			message.append("接口返回：").append(result).append("\n");
			
			JSONObject jsonMap = JSONObject.parseObject(result);
			if (!jsonMap.getBooleanValue("result")) {
				message.insert("BBIN queryStatusByBillno".length(),"查询订单异常！").append("\n");
				telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-bbin queryStatusByBillno",message.toString().replace("&","*"));
				return JSONUtils.map2Json(failure("bbin返回失败 : " + jsonMap.getJSONObject("data").getString("Message")));
			}
			String status = jsonMap.getJSONObject("data").getString("Status");
			if ("1".equals(status)) {
				return JSONUtils.map2Json(success("success!"));
			} else if ("-1".equals(status)) {
				message.insert("BBIN queryStatusByBillno".length(),"查询订单异常！").append("\n");
				telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-bbin queryStatusByBillno",message.toString().replace("&","*"));
				return JSONUtils.map2Json(failure("failure: " + result));
			} else {
				logger.warn("未知状态 = {}", status);
			}
		} catch (Exception e) {
			logger.error("查询订单状态错误 : ", e);
			message.insert("BBIN queryStatusByBillno".length(),"查询订单异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-bbin queryStatusByBillno",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe(result));
		}
		return JSONUtils.map2Json(failure(result));
	}

	/**
	 * 进入单个游戏
	 */
	private String playGame(ApiInfoEntity entity, String username, String password, String gamekind, String gametype, String gamecode, String lang,String loginUrl,String isFlash) {
		String result = "";
		try {
			String key = this.bbinKeyGenerate(BbinConstants.WEB_SITE, username, BbinConstants.PLAY_GAME_KEY, 5, 8);
			BBinTransferVo vo = new BBinTransferVo(BbinConstants.WEB_SITE, username,entity.getAgent(),password,gamekind, gametype, gamecode, lang, key);
			String param = ReflectUtil.generateParam(vo);
			if(StringsUtil.isNull(loginUrl)){
				if("0".equals(isFlash)){
					loginUrl = entity.getBbinUrl() + BbinConstants.function.PLAY_GAME;
				}else{
					loginUrl = entity.getBbinUrl() + BbinConstants.function.H5_GAME;
				}
			}else{
				if("0".equals(isFlash)){
					loginUrl = loginUrl + BbinConstants.function.PLAY_GAME;
				}else{
					loginUrl = loginUrl + BbinConstants.function.H5_GAME;
				}
			}
			result = loginUrl + "?" + param;
			logger.info("bbin playGame siteId={},username={},gamekind={},gameType={},result={}",
					entity.getSiteId(),username,gamekind,gametype,result);
			return result;
		} catch (Exception e) {
			logger.error("bbin进入某个游戏异常 : ", e);
			return null;
		}
	}

	/**
	 * 本地创建会员
	 */
	private BbinApiUserEntity createMemberByLocal(UserParam param, ApiInfoEntity entity, String username, String cur, String oddType) {
		BbinApiUserEntity bbinUser = new BbinApiUserEntity();
		bbinUser.setUsername(username);
		bbinUser.setPassword(entity.getPassword());
		bbinUser.setAgentName(entity.getAgent());
		bbinUser.setApiInfoId(entity.getId().intValue());//api 的 id
		bbinUser.setSiteName(entity.getProjectAgent());
		bbinUser.setCurrencyType(cur);
		bbinUser.setOddtype(oddType);
		bbinUser.setCreateTime(DateUtil.getCurrentTime());//创建时间
		bbinUser.setUserStatus(1);//状态=1 有效会员
		bbinUser.setSiteId(entity.getSiteId());
		bbinUser.setSiteName(entity.getProjectAgent());
		this.bbinApiUserEntityMapper.insert(bbinUser);
		return bbinUser;
	}

	/**
	 * bbin 生成key
	 */
	private String bbinKey(String webSite, String username, String remitno, String keyB) {
		StringBuilder tranBBINkey = new StringBuilder();
		StringBuilder mdBuilder = new StringBuilder();
		mdBuilder.append(webSite).append(username).append(remitno).append(keyB).append(StringsUtil.updateTime());
		tranBBINkey.append(StringsUtil.randomString(8)).append(StringsUtil.toMD5(mdBuilder.toString())).append(StringsUtil.randomString(5));
		return tranBBINkey.toString();
	}

	/**
	 * 查询 bbin 生成key 
	 */
	private String bbinKeyGenerate(String website, String agent, String key, Integer start, Integer end) {
		start = start == null ? 1 : start;
		end = end == null ? 1 : end;
		StringBuilder balanceBuilder = new StringBuilder();
		StringBuilder bbinMd5Builder = new StringBuilder();
		bbinMd5Builder.append(website).append(agent).append(key).append(StringsUtil.updateTime());
		balanceBuilder.append(StringsUtil.randomString(start)).append(StringsUtil.toMD5(bbinMd5Builder.toString())).append(StringsUtil.randomString(end));
		return balanceBuilder.toString();
	}

	/** 查询订单状态加密方式 */
	private String queryOrderStatusKeyGenerate(String website, String keyb) {
		return StringsUtil.randomString(6) + StringsUtil.toMD5(website + keyb + StringsUtil.updateTime()) + StringsUtil.randomString(2);
	}
}
