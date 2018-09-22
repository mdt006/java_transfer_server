package com.ds.transfer.ky.service;
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
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSONObject;
import com.ds.msg.TelegramMessage;
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
import com.ds.transfer.ky.constants.KyConstants;
import com.ds.transfer.ky.constants.TelegramConstants;
import com.ds.transfer.ky.util.Encrypt;
import com.ds.transfer.ky.vo.BaseParams;
import com.ds.transfer.ky.vo.Param;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.KyApiUserEntity;
import com.ds.transfer.record.entity.KyApiUserEntityExample;
import com.ds.transfer.record.entity.TransferOtherRule;
import com.ds.transfer.record.entity.TransferOtherRuleExample;
import com.ds.transfer.record.entity.TransferOtherRuleExample.Criteria;
import com.ds.transfer.record.entity.TransferRecordEntity;
import com.ds.transfer.record.mapper.KyApiUserEntityMapper;
import com.ds.transfer.record.mapper.TransferOtherRuleMapper;
import com.ds.transfer.record.service.TransferRecordService;

/**
* @ClassName: TransferServiceImpl
* @Description: TODO(KY业务接口实现类)
* @author leo
* @date 2018年2月27日
*/
public class TransferServiceImpl extends CommonTransferService implements TransferService<KyApiUserEntity> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;
	@Resource
	private KyApiUserEntityMapper kyApiUserEntityMapper;
	@Resource
	private TransferOtherRuleMapper transferOtherRuleMapper;
	@Autowired
	protected RedisTemplate<String, String> redisTemplate;
	//小飞机消息组件
	TelegramMessage telegramMessage = TelegramMessage.getInstance();

	@Override
	public String transfer(TransferParam transferParam) {
		StringBuilder message = new StringBuilder();
		TransferRecordEntity dsRecord = new TransferRecordEntity();
		ApiInfoEntity entity = transferParam.getEntity();
		String msg = null;
		try {
			String timestamp = DateUtil.getUTCCurrentTime();
			String url = entity.getReportUrl();
			String agent = entity.getAgent();
			String platformUser =entity.getPrefix().concat(transferParam.getUsername());
			
			message.append("KY Transfer 站点：").append(entity.getSiteId());
			message.append(",会员：").append(platformUser);
			message.append(",转账：").append(transferParam.getType().equals("IN")==true? "DS-->KY" : "KY-->DS");
			message.append(",单号:").append(transferParam.getBillno());
			message.append(",金额：").append(transferParam.getCredit()).append("\n");
			
			String kyBillno = getKyBillno(agent,platformUser);
			//插入ky转账记录
			dsRecord = this.transferRecordService.insert(entity.getSiteId(), entity.getLiveId(), transferParam.getTransRecordId(), entity.getPassword(),
					transferParam.getUsername(), transferParam.getCredit(), transferParam.getBillno(),transferParam.getType(), KyConstants.KY, transferParam.getRemark(), dsRecord);
			logger.info("ky 转账记录插入成功,username={},siteId={},id = {}",transferParam.getUsername(),entity.getSiteId(),dsRecord.getId());
			
			//由于第三方转账单号和其它平台单号冲突此处做映射处理
			logger.info("ky 转账记录开始插入映射表username={},siteId={},billno={},kyBillno={}",transferParam.getUsername(),entity.getSiteId(),transferParam.getBillno(),kyBillno);
			this.saveTransferOtherRuleMapper(platformUser, entity.getSiteId(), entity.getLiveId(),entity.getLiveName(),
					transferParam.getBillno(),transferParam.getCredit(), kyBillno, transferParam.getTransRecordId(),transferParam.getType());
			logger.info("ky 转账记录插入映射表成功,username={},siteId={},billno = {},kyBillno={}",transferParam.getUsername(),entity.getSiteId(),transferParam.getBillno(),kyBillno);
			
			String type = IN.equals(transferParam.getType()) ? KyConstants.IN : KyConstants.OUT;
			Param params = new Param(type,platformUser,transferParam.getCredit(),kyBillno);
			String paramStr = ReflectUtil.generateParam(params, KyConstants.KY_PARAM_JOIN);
			logger.info("ky transfer before username={},siteId={},url ={},明文参数param={}",transferParam.getUsername(),entity.getSiteId(),url,paramStr);
			message.append("请求地址：").append(url).append("\n");
			message.append("明文参数：").append(paramStr).append("\n");
			String encryptParam = Encrypt.AESEncrypt(paramStr,KyConstants.DES_KEY);
			String key =Encrypt.MD5(agent.concat(timestamp).concat(KyConstants.MD5_KEY));
			BaseParams baseParam = new BaseParams(agent,encryptParam,timestamp,key);
			String sendParam = ReflectUtil.generateParam(baseParam, KyConstants.KY_PARAM_JOIN);
			message.append("密文参数：").append(sendParam).append("\n");
			logger.info("ky transfer before username={},siteId={},url ={},param={}",transferParam.getUsername(),entity.getSiteId(),url,sendParam);
			
			long startTime = System.currentTimeMillis();
			String result = StringsUtil.sendGet(url,sendParam);
			long endTime = System.currentTimeMillis();
			message.append("接口返回：").append(result);
			
			logger.info("ky transfer after username={},siteId={},elapsedTime={} ms,result={}",transferParam.getUsername(),entity.getSiteId(),(endTime-startTime),result);
			JSONObject resultObj = JSONObject.parseObject(result);
			if (resultObj.containsKey("d") && "0".equals(resultObj.getJSONObject("d").getString("code"))) {
				logger.info("ky transfer success username={},siteId={},type= {}",transferParam.getUsername(),entity.getSiteId(),transferParam.getType().equals("IN")==true?"DS->KY":"KY->DS");
				dsRecord = this.transferRecordService.update(SysConstants.Record.TRANS_SUCCESS, "KY转账成功", dsRecord);
				return JSONUtils.map2Json(success("KY转账成功"));
			}
			logger.info("ky transfer after username={},siteId={} transfer error!",transferParam.getUsername(),entity.getSiteId());
			dsRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "ky转账异常", dsRecord);
			
			message.insert("KY Transfer".length(),"转账异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ky transfer",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("KY转账异常"));
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("ky transfer username={},siteId={},billno={} transfer error：={}!",transferParam.getUsername(),entity.getSiteId(),transferParam.getBillno(),e);
			dsRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "ky转账异常", dsRecord);
			message.insert("KY Transfer".length(),"转账异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ky transfer",message.toString().replace("&", "*"));
			return JSONUtils.map2Json(maybe("system error !" + msg));
		}
	}
	
	@Override
	public String queryBalance(QueryBalanceParam param) {
		StringBuilder message = new StringBuilder();
		try {
			//开元棋牌第三方接口转账余额延迟问题，线程休眠等待2秒再查询
			Thread.sleep(2000);
			ApiInfoEntity entity = param.getEntity();
			String platformUser= entity.getPrefix().concat(param.getUsername());
			String agent = entity.getAgent();
			String url = entity.getReportUrl();
			String siteId = String.valueOf(entity.getSiteId());
			String timestamp = DateUtil.getUTCCurrentTime();
			
			message.append("KY qeuryBalance 站点：").append(entity.getSiteId());
			message.append("\n会员：").append(param.getUsername()+",");
			
			Param params = new Param(KyConstants.BALANCE,platformUser,siteId);
			String paramStr = ReflectUtil.generateParam(params, KyConstants.KY_PARAM_JOIN);
			message.append("请求地址：").append(url).append("\n");
			message.append("明文参数：").append(paramStr).append("\n");
			
			String encryptParams = Encrypt.AESEncrypt(paramStr,KyConstants.DES_KEY);
			String key =Encrypt.MD5(agent.concat(String.valueOf(timestamp)).concat(KyConstants.MD5_KEY));
			BaseParams baseParams = new BaseParams(agent,encryptParams,timestamp,key);
			String sendParam = ReflectUtil.generateParam(baseParams, KyConstants.KY_PARAM_JOIN);
			message.append("密文文参数：").append(sendParam).append("\n");
			
			logger.info("ky queryBalance before username={},siteId={},param={}",param.getUsername(),siteId,sendParam);
			long startTime = System.currentTimeMillis();
			String result = StringsUtil.sendGet(url,sendParam);
			long endTime = System.currentTimeMillis();
			message.append("接口返回：").append(result).append("\n");
			logger.info("ky queryBalance after username={},siteId={},elapsedTime={} ms,result={}",param.getUsername(),siteId,(endTime-startTime),result);
			JSONObject resultObj = JSONObject.parseObject(result);
			
			if (resultObj.containsKey("d") && "0".equals(resultObj.getJSONObject("d").getString("code"))) {
				String balance = resultObj.getJSONObject("d").getString("money");
				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(STATUS, SUCCESS);
				resultMap.put("balance", balance);
				//正则判断值是否标准金额
				if(BigDemicalUtil.checkoutMoney(balance)){
					//查询到的余额添加到缓存，用于批量查询,缓存7天自动清除
					this.redisTemplate.opsForValue().set(entity.getLiveId()+"_"+entity.getSiteId()+"_"+platformUser, balance);
					this.redisTemplate.expire(entity.getLiveId()+"_"+entity.getSiteId()+"_"+platformUser,60*24*7, TimeUnit.MINUTES);
				}
				return JSONUtils.map2Json(resultMap);
			}
			
			message.insert("KY qeuryBalance".length(),"查询余额异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ky login",message.toString().replace("&","*"));
			return JSONUtils.map2Json(failure(result));
		} catch (Exception e) {
			e.printStackTrace();
			message.insert("KY qeuryBalance".length(),"查询余额异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ky login",message.toString().replace("&","*"));
			logger.error("ky qeuryBalance username={},siteId={},error:={}!",param.getUsername(),param.getEntity().getSiteId(),e);
			return JSONUtils.map2Json(maybe("查询余额失败"));
		}
	}

	@Override
	public String login(LoginParam param) {
		StringBuilder message = new StringBuilder();
		String result = null;
		ApiInfoEntity entity = param.getEntity();
		String platformUser = entity.getPrefix().concat(param.getUsername());
		String money = "0";
		try {
			message.append("KY login 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername()).append("\n");
			
			String gameCode = param.getGamecode();
			String ip = param.getIp();
			String url = entity.getReportUrl();
			String agent = entity.getAgent();
			String timestamp = DateUtil.getUTCCurrentTime();
			String orderid = agent.concat(timestamp).concat(platformUser);
			String lineCode= String.valueOf(entity.getSiteId());
			String KindID =  StringsUtil.isNull(gameCode) == true?"0":gameCode;
			Param params = new Param(KyConstants.LOGIN,platformUser,money,orderid,ip,lineCode);
			String paramStr = ReflectUtil.generateParam(params).concat("&KindID=").concat(KindID);
			message.append("请求地址：").append(url).append("\n");
			message.append("明文参数：").append(paramStr).append("\n");
			logger.info("ky login before username={},siteId={},明文:param={}",platformUser,entity.getSiteId(),paramStr);
			String encryptParams = Encrypt.AESEncrypt(paramStr,KyConstants.DES_KEY);
			String key =Encrypt.MD5(agent.concat(String.valueOf(timestamp)).concat(KyConstants.MD5_KEY));
			BaseParams baseParams = new BaseParams(agent,encryptParams,timestamp,key);
			String baseParamsStr = ReflectUtil.generateParam(baseParams);
			message.append("密文参数：").append(baseParamsStr).append("\n");
			logger.info("ky login before username={},siteId={},密文：param={}",param.getUsername(),lineCode,baseParamsStr);
			long startTime = System.currentTimeMillis();
			result = StringsUtil.sendGet(url, baseParamsStr);
			long endTime = System.currentTimeMillis();
			message.append("接口返回：").append(result).append("\n");
			logger.info("ky login after username={},siteId={},elapsedTime={} ms,result={}",param.getUsername(),entity.getSiteId(),(endTime-startTime),result);
			JSONObject jsonMap = JSONObject.parseObject(result);
			if(jsonMap.containsKey("d")){
				JSONObject contentJson = jsonMap.getJSONObject("d");
				if ("0".equals(contentJson.getString("code"))) {
					return JSONUtils.map2Json(success(contentJson.getString("url")));
				}else{
					return JSONUtils.map2Json(failure("登录异常！"));
				}
			}
			message.insert("KY login".length(),"登录异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ky login",message.toString().replace("&", "*"));
			return JSONUtils.map2Json(failure(jsonMap.toJSONString()));
		} catch (Exception e) {
			message.insert("KY login".length(),"登录异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ky login",message.toString().replace("&","*"));
			logger.error("ky login username={},siteId={}, error:={}!",param.getUsername(),entity.getSiteId(),e);
			e.printStackTrace();
			return JSONUtils.map2Json(maybe("system error : " + result));
		}
	}
	
	/**
	 * @param param
	 * @return
	 */
	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		StringBuilder message = new StringBuilder();
		ApiInfoEntity entity = param.getEntity();
		String timestamp = DateUtil.getUTCCurrentTime();
		String kyBillno = "";
		String sendParam = "";
		try {
			String agent = entity.getAgent();
			String url = entity.getReportUrl();
			
			message.append("KY queryStatusByBillno 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			TransferOtherRuleExample transferOtherRule = new TransferOtherRuleExample();
			Criteria createCriteria = transferOtherRule.createCriteria();
			createCriteria.andTransCenterBillnoEqualTo(param.getBillno());
			createCriteria.andSiteIdEqualTo(param.getEntity().getSiteId());
			createCriteria.andLiveIdEqualTo(param.getEntity().getLiveId());
			List<TransferOtherRule> transferOtherList = this.transferOtherRuleMapper.selectByExample(transferOtherRule);
			if(transferOtherList.size() > 0 && !StringsUtil.isNull(transferOtherList.get(0).getTransMappingBillno())){
				kyBillno = transferOtherList.get(0).getTransMappingBillno();
			}else{
				kyBillno = param.getBillno();
			}
			StringBuilder kyParam = new StringBuilder();
			kyParam.append("s=").append(KyConstants.CHECK);
			kyParam.append("&orderid=").append(kyBillno);
			logger.info("ky queryStatusByBillno before username={},siteId={},param={}",param.getUsername(),entity.getSiteId(),kyParam);
			message.append("请求地址：").append(url).append("\n");
			message.append("明文参数：").append(kyParam).append("\n");
			String params = Encrypt.AESEncrypt(kyParam.toString(),KyConstants.DES_KEY);
			String key =Encrypt.MD5(agent.concat(timestamp).concat(KyConstants.MD5_KEY));
			BaseParams sendParams = new BaseParams(agent,params,timestamp,key);
			sendParam = ReflectUtil.generateParam(sendParams);
			message.append("密文参数：").append(sendParam).append("\n");
			logger.info("ky queryStatusByBillno before username={},siteId={},url={},param={}",param.getUsername(),entity.getSiteId(),url,sendParam);
			long startTime = System.currentTimeMillis();
			String result = StringsUtil.sendGet(url,sendParam.toString());
			long endTime = System.currentTimeMillis();
			message.append("接口返回：").append(result).append("\n");
			logger.info("ky queryStatusByBillno after username={},siteId={},billno={},elapsedTime={} ms,result={}",param.getUsername(),entity.getSiteId(),kyBillno,(endTime-startTime),result);
			JSONObject resultObj = JSONObject.parseObject(result);
			if(resultObj.containsKey("d")){
				JSONObject jsonObject = resultObj.getJSONObject("d");
				String status = jsonObject.getString("status");
				if("0".equals(jsonObject.getString("code")) && "0".equals(status)){
					return JSONUtils.map2Json(success("success!"));
				}
				if("0".equals(jsonObject.getString("code")) && "-1".equals(status)){
					message.insert("KY queryStatusByBillno".length(),"查询此订单不存在！").append("\n");
					telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ky queryStatusByBillno",message.toString().replace("&", "*"));
					return JSONUtils.map2Json(failure(result));
				}
				if("0".equals(jsonObject.getString("code")) && "2".equals(status)){
					message.insert("KY queryStatusByBillno".length(),"查询此订单为失败！").append("\n");
					telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ky queryStatusByBillno",message.toString().replace("&", "*"));
					return JSONUtils.map2Json(failure(result));
				}
			}
			message.insert("KY queryStatusByBillno".length(),"查询订单异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ky queryStatusByBillno",message.toString().replace("&", "*"));
			return JSONUtils.map2Json(maybe(result));
		} catch (Exception e) {
			message.insert("KY queryStatusByBillno".length(),"查询订单异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ky queryStatusByBillno",message.toString().replace("&", "*"));
			logger.info("ky queryStatusByBillno username={},siteId={},billno={},error:{}!",param.getUsername(),entity.getSiteId(),kyBillno,e);
			e.printStackTrace();
			return JSONUtils.map2Json(maybe("system error!"));
		}
	}
	
	@Override
	public String queryAgentBalance(QueryBalanceParam param) {
		logger.info("ky queryAgentBalance username={},siteId={},agent={}",param.getUsername(),param.getEntity().getSiteId(),param.getAgent());
		Map<String, Object> resultMap = null;
		double balance = 0;  
		try {
			String siteId = String.valueOf(param.getEntity().getSiteId());
			logger.info("查询redis缓存代理总额度 agent{}, siteId{}",param.getAgent(),siteId);
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
			logger.info("ky queryAgentBalance username={},siteId={},agent={},totalBalance={}",param.getUsername(),param.getEntity().getSiteId(),param.getAgent(),balance);
			return JSONUtils.map2Json(resultMap);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("ky queryAgentBalance username={},siteId={},agent={},error:{}!",param.getUsername(),param.getEntity().getSiteId(),param.getAgent(),e);
			resultMap = maybe("查询余额异常");
		}
		return JSONUtils.map2Json(resultMap);
	}
	
	@Override
	@Transactional
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		logger.info("ky checkAndCreateMember username={},siteId={}",param.getUsername(),param.getEntity().getSiteId());
		Map<String, Object> resultMap = new HashMap<String,Object>();
		Integer siteId = null;
		ApiInfoEntity entity = param.getEntity();
		String password = entity.getPassword();
		String platformUser =entity.getPrefix().concat(param.getUsername());
		
		try {
			String cur = StringsUtil.isNull(param.getCur()) ? SysConstants.LANGUAGE : param.getCur();
			String oddType = StringsUtil.isNull(param.getOddtype()) ? SysConstants.CUR : param.getOddtype();
			siteId = entity.getSiteId();
			//查询本地是否存在该用户
			KyApiUserEntity user = this.queryUserExist(platformUser);
			if (user == null) {
				//1.创建ky会员
				LoginParam loginParam = new LoginParam(entity,param.getUsername());
				logger.info("ky network create user before username={},siteId={}",param.getUsername(),siteId);
				resultMap = JSONUtils.json2Map(this.login(loginParam));
				logger.info("ky network create user after username={},siteId={}",param.getUsername(),siteId,resultMap.toString());
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					//2.创建本地会员
					user = this.createMemberByLocal(entity,platformUser,password,cur,oddType);
				}
				return resultMap;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("ky network create user username={},siteId={},error:{}!",platformUser,String.valueOf(siteId),e);
			return maybe("创建KY会员异常");
		}
		return success("用户已经存在");
	}
	
	@Override
	public KyApiUserEntity queryUserExist(String username) {
		KyApiUserEntityExample kyApiUserExample = new KyApiUserEntityExample();
		kyApiUserExample.createCriteria().andUsernameEqualTo(username);
		List<KyApiUserEntity> list = this.kyApiUserEntityMapper.selectByExample(kyApiUserExample);
		return (list == null || list.size() == 0) ? null : list.get(0);
	}
	
	/**
	 * 本地创建会员
	 */
	private KyApiUserEntity createMemberByLocal(ApiInfoEntity entity, String username, String password, String cur, String oddtype) {
		KyApiUserEntity kyUser = new KyApiUserEntity();
		kyUser.setUsername(username);
		kyUser.setPassword(password);
		kyUser.setAgentName(entity.getAgent());
		kyUser.setApiInfoId(entity.getId().intValue());//api 的 id
		kyUser.setSiteName(entity.getProjectAgent());
		kyUser.setOddtype(oddtype);
		kyUser.setCurrencyType(cur);
		kyUser.setCreateTime(DateUtil.getCurrentTime());//创建时间
		kyUser.setUserStatus(1);//状态=1 有效会员
		kyUser.setSiteId(entity.getSiteId());
		kyUser.setSiteName(entity.getProjectAgent());
		this.kyApiUserEntityMapper.insert(kyUser);//插入 ky会员资料
		return kyUser;
	}
	
	
	private void saveTransferOtherRuleMapper(String username,Integer siteId,Integer liveId,String liveType,String transCenterBillno,
			String transferMoney,String transMappingBillno,Long transRecordId,String transType){
		logger.info("ky transfer save transfer before mapping table username={},siteid={},billno={}",username,siteId,transCenterBillno);
		try {
			TransferOtherRule transferOtherRule = new TransferOtherRule();
			transferOtherRule.setUsername(username);
			transferOtherRule.setLiveId(liveId);
			transferOtherRule.setLiveType(liveType);
			transferOtherRule.setSiteId(siteId);
			transferOtherRule.setTransCenterBillno(transCenterBillno);
			transferOtherRule.setTransferMoney(transferMoney);
			transferOtherRule.setTransMappingBillno(transMappingBillno);
			transferOtherRule.setTransRecordId(transRecordId);
			transferOtherRule.setTransType(transType);
			transferOtherRule.setCreateTime(DateUtil.getCurrentTime());
			transferOtherRule.setUpdateTime(DateUtil.getCurrentTime());
			this.transferOtherRuleMapper.insert(transferOtherRule);
			logger.info("ky transfer save transfer after mapping table username={},siteid={},billno={} success",username,siteId,transCenterBillno);
		} catch (Exception e) {
			logger.info("ky transfer save mapping table username={},siteid={},billno={} error！",username,siteId,transCenterBillno);
			e.printStackTrace();
			//抛出异常到上层此处用于日志打印错误日志
			throw e;
		}
	}
	
	private String getKyBillno(String agent,String platformUser){
		return agent.concat(DateUtil.getUTCCurrentTimeFormat()).concat(platformUser);
	}
	
	@Override
	public String loginBySingGame(LoginParam param) {
		return null;
	}
}
