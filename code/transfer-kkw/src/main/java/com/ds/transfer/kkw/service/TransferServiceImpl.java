package com.ds.transfer.kkw.service;
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
import com.ds.transfer.common.util.StringsUtil;
import com.ds.transfer.common.vo.LoginParam;
import com.ds.transfer.common.vo.QueryBalanceParam;
import com.ds.transfer.common.vo.QueryOrderStatusParam;
import com.ds.transfer.common.vo.TransferParam;
import com.ds.transfer.common.vo.UserParam;
import com.ds.transfer.kkw.constants.KkwConstants;
import com.ds.transfer.kkw.constants.TelegramConstants;
import com.ds.transfer.kkw.vo.LoginVo;
import com.ds.transfer.kkw.vo.MoneyVo;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.KkwApiUserEntity;
import com.ds.transfer.record.entity.KkwApiUserEntityExample;
import com.ds.transfer.record.entity.TransferRecordEntity;
import com.ds.transfer.record.mapper.KkwApiUserEntityMapper;
import com.ds.transfer.record.service.TransferRecordService;

/**
* @ClassName: TransferServiceImpl
* @Description: TODO(kkw业务接口实现类)
* @author leo
* @date 2018年7月11日
*/
public class TransferServiceImpl extends CommonTransferService implements TransferService<KkwApiUserEntity> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;
	@Resource
	private KkwApiUserEntityMapper kkwApiUserEntityMapper;
	@Autowired
	protected RedisTemplate<String, String> redisTemplate;
	//小飞机消息组件
	TelegramMessage telegramMessage = TelegramMessage.getInstance();
	
	@Override
	public String transfer(TransferParam transferParam) {
		TransferRecordEntity dsRecord = new TransferRecordEntity();
		StringBuilder message = new StringBuilder();
		ApiInfoEntity entity = transferParam.getEntity();
		String msg = null;
		String prefix= "";
		String result = "";
		try {
			String url = entity.getReportUrl();
			if(!StringsUtil.isNull(entity.getPrefix())){
				prefix = entity.getPrefix();
			}
			String platformUser = prefix.concat(transferParam.getUsername());
			
			message.append("KKW Transfer 站点：").append(entity.getSiteId());
			message.append(",会员：").append(platformUser);
			message.append(",转账：").append(transferParam.getType().equals("IN")==true ? "DS-->KKW" : "KKW-->DS");
			message.append(",单号:").append(transferParam.getBillno());
			message.append(",金额：").append(transferParam.getCredit()).append("\n");
			
			//插入Ds转账记录
			dsRecord = this.transferRecordService.insert(entity.getSiteId(), entity.getLiveId(), transferParam.getTransRecordId(), entity.getPassword(),
					transferParam.getUsername(), transferParam.getCredit(), transferParam.getBillno(),transferParam.getType(), KkwConstants.KKW, transferParam.getRemark(), dsRecord);
			logger.info("kkw 转账记录插入成功,username={},siteId={},id = {}",transferParam.getUsername(),entity.getSiteId(),dsRecord.getId());
			MoneyVo vo = new MoneyVo(platformUser, entity.getPassword(), transferParam.getBillno(),"ds钱包转" + (IN.equals(transferParam.getType()) ? "入" : "出"),transferParam.getCredit());
			String paramBody = JSONUtils.bean2Json(vo);
			
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("hashCode", entity.getHashcode());
			param.put("command", (IN.equals(transferParam.getType()) ? KkwConstants.IN : KkwConstants.OUT));
			param.put("params", JSONObject.parse(paramBody));
			logger.info("kkw transfer before username={},siteId={},url ={},param={}",transferParam.getUsername(),entity.getSiteId(),url,param.toString());
			
			message.append(" 转账地址：").append(url).append("\n");
			message.append(" 转账参数：").append(param).append("\n");
			long startTime = System.currentTimeMillis();
			result = StringsUtil.sendPost1(url, JSONUtils.map2Json(param));
			long endTime = System.currentTimeMillis();
			message.append(" 接口返回：").append(result).append("\n");
			
			logger.info("kkw transfer after username={},siteId={},elapsedTime={} ms,result={}",transferParam.getUsername(),entity.getSiteId(),(endTime-startTime),result);
			JSONObject resultObj = JSONObject.parseObject(result);
			if ("0".equals(resultObj.getString("errorCode"))) {
				logger.info("kkw transfer success username={},siteId={},type= {}",transferParam.getUsername(),entity.getSiteId(),transferParam.getType().equals("IN")==true?"DS->KKW":"KKW->DS");
				dsRecord = this.transferRecordService.update(SysConstants.Record.TRANS_SUCCESS, "KKW转账成功", dsRecord);
				return JSONUtils.map2Json(success("kkw转账成功"));
			}
			logger.info("kkw transfer after username={},siteId={} transfer error!",transferParam.getUsername(),entity.getSiteId());
			dsRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "KKW转账失败", dsRecord);
			message.insert("KKW Transfer".length(),"转账异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-kkw transfer",message.toString().replace("&","*"));
			return JSONUtils.map2Json(failure("ds转账失败"));
		} catch (Exception e) {
			logger.info("kkw transfer username={},siteId={} transfer error：={}!",transferParam.getUsername(),entity.getSiteId(),e);
			dsRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "KKW转账异常", dsRecord);
			message.insert("KKW Transfer".length(),"转账异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-kkw transfer",message.toString().replace("&","*"));
			e.printStackTrace();
			return JSONUtils.map2Json(maybe("system error !" + msg));
		}
	}
	
	@Override
	public String queryBalance(QueryBalanceParam param) {
		StringBuilder message = new StringBuilder();
		ApiInfoEntity entity = null;
		String prefix = "";
		String result = "";
		try {
			entity = param.getEntity();
			if(!StringsUtil.isNull(entity.getPrefix())){
				prefix = entity.getPrefix();
			}
			String platformUser= prefix.concat(param.getUsername());
			
			message.append("KKW qeuryBalance 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername()+",");
			
			Map<String, Object> DsParam = new HashMap<>();
			DsParam.put("hashCode", entity.getHashcode());
			DsParam.put("command",KkwConstants.BALANCE);
			String url = entity.getReportUrl();
			Map<String, Object> paramBody = new HashMap<>();
			paramBody.put("username",platformUser);
			paramBody.put("password", entity.getPassword());
			DsParam.put("params", paramBody);
			String sendParam = JSONUtils.map2Json(DsParam);
			logger.info("kkw queryBalance before username={},siteId={},param={}",param.getUsername(),entity.getSiteId(),sendParam);
			
			message.append("请求地址：").append(url).append("\n");
			message.append("请求参数：").append(sendParam).append("\n");
			long startTime = System.currentTimeMillis();
			result = StringsUtil.sendPost1(url, sendParam);
			long endTime = System.currentTimeMillis();
			message.append("接口返回：").append(result).append("\n");
			logger.info("kkw queryBalance after username={},siteId={},elapsedTime={} ms,result={}",param.getUsername(),entity.getSiteId(),(endTime-startTime),result);
			JSONObject resultObj = JSONObject.parseObject(result);
			if ("0".equals(resultObj.getString("errorCode"))) {
				String balance = resultObj.getJSONObject("params").getString("balance");
				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(STATUS, SUCCESS);
				resultMap.put(MESSAGE,balance);
				resultMap.put("balance",balance);
				//正则判断值是否标准金额
				if(BigDemicalUtil.checkoutMoney(balance)){
					//查询到的余额添加到缓存，用于批量查询,缓存7天自动清除
					this.redisTemplate.opsForValue().set(entity.getLiveId()+"_"+entity.getSiteId()+"_"+platformUser, balance);
					this.redisTemplate.expire(entity.getLiveId()+"_"+entity.getSiteId()+"_"+platformUser,60*24*7, TimeUnit.MINUTES);
				}
				return JSONUtils.map2Json(resultMap);
			}
			message.insert("KKW qeuryBalance".length(),"查询余额异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-kkw qeuryBalance",message.toString().replace("&", "*"));
			return JSONUtils.map2Json(failure(result));
		} catch (Exception e) {
			logger.error("kkw qeuryBalance username={},siteId={},error:={}!",param.getUsername(),param.getEntity().getSiteId(),e);
			e.printStackTrace();
			message.insert("KKW qeuryBalance".length(),"查询余额异常！").append("\n");
			message.append("异常信息：").append(e.toString());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-kkw qeuryBalance",message.toString().replace("&", "*"));
			return JSONUtils.map2Json(maybe("查询余额失败"));
		}
	}

	@Override
	public String login(LoginParam param) {
		StringBuilder message = new StringBuilder();
		String result = null;
		String prefix = "";
		ApiInfoEntity entity = param.getEntity();
		if(!StringsUtil.isNull(entity.getPrefix())){
			prefix = entity.getPrefix();
		}
		String platformUser = prefix.concat(param.getUsername());
		try {
			String gameType = param.getGameType();
			String line = param.getLine();
			
			message.append("KKW login 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			LoginVo vo = new LoginVo(entity.getHashcode(),KkwConstants.LOGIN);
			LoginVo.Param params = vo.new Param(platformUser, entity.getPassword(), StringsUtil.isNull(param.getCur()) ? entity.getCurrencyType() : param.getCur(),
					platformUser, param.getLanguage(), Integer.valueOf(line),gameType);
			vo.setParams(params);
			String loginParam = JSONUtils.bean2Json(vo);
			String url = entity.getReportUrl();
			logger.info("kkw login before username={},siteId={},url={},param={}",param.getUsername(),entity.getSiteId(),url,loginParam);
			
			message.append("请求地址：").append(url).append("\n");
			message.append("请求参数：").append(loginParam).append("\n");
			long startTime = System.currentTimeMillis();
			result = StringsUtil.sendPost1(url, loginParam);
			long endTime = System.currentTimeMillis();
			message.append("接口返回：").append(result).append("\n");
			
			logger.info("kkw login after username={},siteId={},elapsedTime={} ms,result={}",param.getUsername(),entity.getSiteId(),(endTime-startTime),result);
			JSONObject jsonMap = JSONObject.parseObject(result);
			if ("0".equals(jsonMap.getString("errorCode"))) {
				result = jsonMap.getJSONObject("params").getString("link");
				return JSONUtils.map2Json(success(result));
			}
			
			if("6606".equals(jsonMap.getString("errorCode"))){
				logger.info("username={},siteId={},密码不正确，开始重置密码！",platformUser,entity.getSiteId());
				Map<String,Object> paramsMap = new HashMap<String,Object>();
				paramsMap.put("username",platformUser);
				paramsMap.put("password",entity.getPassword());
				Map<String,Object> paramMap = new HashMap<String,Object>();
				paramMap.put("hashCode",entity.getHashcode());
				paramMap.put("command", "CHANGE_PASSWORD");
				paramMap.put("params", paramsMap);
				logger.info("kkw before chancePassword username={},siteId={}, param={}",platformUser,entity.getSiteId(),loginParam);
				String chancePasswordResult = StringsUtil.sendPost1(url, JSONUtils.map2Json(paramMap));
				JSONObject jsonObject = JSONObject.parseObject(chancePasswordResult);
				logger.info("kkw after chancePassword username={},siteId={}, result={}",platformUser,entity.getSiteId(),chancePasswordResult);
				if(jsonObject.containsKey("errorCode") && 0 == jsonObject.getInteger("errorCode")){
					Thread.sleep(2000);
					logger.info("username={},siteId={},重置密码成功！",platformUser,entity.getSiteId());
					this.login(param);
				}
			}
		} catch (Exception e) {
			message.insert("KKW login".length(),"登录异常！").append("\n");
			message.append("异常信息：").append(e.toString());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-kkw login",message.toString());
			logger.error("kkw login username={},siteId={} error:{}",param.getUsername(),entity.getSiteId(),e);
			return JSONUtils.map2Json(maybe("system error : " + result));
		}
		return JSONUtils.map2Json(failure(result));
	}
	
	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		StringBuilder message = new StringBuilder();
		ApiInfoEntity entity = param.getEntity();
		String result = "";
		try {
			message.append("KKW queryStatusByBillno 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			String url = entity.getReportUrl();
			Map<String, Object> DsParam = new HashMap<>();
			DsParam.put("hashCode", entity.getHashcode());
			DsParam.put("command",KkwConstants.CHECK);
			
			Map<String, Object> paramBody = new HashMap<>();
			paramBody.put("ref", param.getBillno());
			DsParam.put("params", paramBody);
			String sendParam = JSONUtils.map2Json(DsParam);
			message.append("请求地址：").append(url).append("\n");
			message.append("请求参数：").append(sendParam).append("\n");
			logger.info("kkw queryStatusByBillno before username={},siteId={},url={},param={}",param.getUsername(),entity.getSiteId(),url,sendParam);
			long startTime = System.currentTimeMillis();
			result = StringsUtil.sendPost1(url,sendParam);
			long endTime = System.currentTimeMillis();
			logger.info("kkw queryStatusByBillno after username={},siteId={},elapsedTime={} ms,result={}",param.getUsername(),entity.getSiteId(),(endTime-startTime),result);
			message.append("接口返回：").append(result);
			
			JSONObject resultObj = JSONObject.parseObject(result);
			if ("6601".equals(resultObj.getString("errorCode"))) {
				return JSONUtils.map2Json(success("success!"));
			} else if ("6617".equals(resultObj.getString("errorCode"))) {
				message.insert("KKW queryStatusByBillno".length(),"查询订单状态异常！").append("\n");
				telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-h8 queryStatusByBillno",message.toString().replace("&","*"));
				return JSONUtils.map2Json(maybe("billno = " + param.getBillno() + " is processing!"));
			} else {
				return JSONUtils.map2Json(failure(result));
			}
		} catch (Exception e) {
			logger.info("kkw queryStatusByBillno username={},siteId={},url={},param={} error:{}!",param.getUsername(),entity.getSiteId(),e);
			e.printStackTrace();
			message.insert("KKW queryStatusByBillno".length(),"查询订单状态异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-kkw queryStatusByBillno",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("system error!"));
		}
	}
	
	@Override
	public String queryAgentBalance(QueryBalanceParam param) {
		logger.info("kkw queryAgentBalance username={},siteId={},agent={}",param.getUsername(),param.getEntity().getSiteId(),param.getAgent());
		Map<String, Object> resultMap = null;
		double balance = 0;  
		try {
			String siteId = String.valueOf(param.getEntity().getSiteId());
			logger.info("kkw 查询redis缓存代理总额度 agent{}, siteId{}",param.getAgent(),siteId);
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
			logger.info("kkw queryAgentBalance username={},siteId={},agent={},totalBalance={}",param.getUsername(),param.getEntity().getSiteId(),param.getAgent(),balance);
			return JSONUtils.map2Json(resultMap);
		} catch (Exception e) {
			logger.info("kkw queryAgentBalance username={},siteId={},agent={},error:{}!",param.getUsername(),param.getEntity().getSiteId(),param.getAgent(),e);
			e.printStackTrace();
			resultMap = maybe("查询余额异常");
		}
		return JSONUtils.map2Json(resultMap);
	}
	
	@Override
	@Transactional
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		logger.info("kkw checkAndCreateMember username={},siteId={}",param.getUsername(),param.getEntity().getSiteId());
		Map<String, Object> resultMap = new HashMap<String,Object>();
		String cur = "";
		ApiInfoEntity entity = param.getEntity();
		String password = entity.getPassword();
		String username = param.getUsername();
		String prefix = StringsUtil.isNull(entity.getPrefix())? "" : entity.getPrefix();
		String platformUser = prefix.concat(username);
		String siteIdUser = String.valueOf(entity.getSiteId()).concat("|").concat(platformUser);
		Integer siteId =  entity.getSiteId();
		
		try {
			cur = StringsUtil.isNull(param.getCur()) ? entity.getCurrencyType() : param.getCur();
			String oddType = StringsUtil.isNull(param.getOddtype()) ? SysConstants.CUR : param.getOddtype();
			//查询本地是否存在该用户
			KkwApiUserEntity user = this.queryUserExist(siteIdUser);
			if (user == null) {
				//1.创建kkw会员
				LoginParam loginParam = new LoginParam(entity,username);
				loginParam.setLine("0"); //默认是用线路0
				loginParam.setGameType(KkwConstants.GAME_TYPE);
				loginParam.setCur(cur);
				logger.info("kkw network create user before username={},siteId={}",username,siteId);
				resultMap = JSONUtils.json2Map(this.login(loginParam));
				logger.info("kkw network create user after username={},siteId={}",username,siteId,resultMap.toString());
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					//2.创建本地会员
					user = this.createMemberByLocal(entity,platformUser,password,cur,oddType);
				}
				return resultMap;
			}
		} catch (Exception e) {
			logger.info("kkw network create user username={},siteId={},error:{}!",username,String.valueOf(siteId),e);
			e.printStackTrace();
			return maybe("创建kkw会员异常");
		}
		return success("用户已经存在");
	}
	
	@Override
	public KkwApiUserEntity queryUserExist(String siteIdUser) {
		String[] siteUser = siteIdUser.split("\\|");
		KkwApiUserEntityExample kkwApiUserEntityExample = new KkwApiUserEntityExample();
		kkwApiUserEntityExample.createCriteria().andUsernameEqualTo(siteUser[1]).andSiteIdEqualTo(Integer.parseInt(siteUser[0]));
		List<KkwApiUserEntity> list = this.kkwApiUserEntityMapper.selectByExample(kkwApiUserEntityExample);
		return (list == null || list.size() == 0) ? null : list.get(0);
	}
	
	/**
	 * 本地创建会员
	 */
	private KkwApiUserEntity createMemberByLocal(ApiInfoEntity entity, String username, String password, String cur, String oddtype) {
		KkwApiUserEntity kkwInsertUser = new KkwApiUserEntity();
		kkwInsertUser.setUsername(username);
		kkwInsertUser.setPassword(password);
		kkwInsertUser.setAgentName(entity.getAgent());
		kkwInsertUser.setApiInfoId(entity.getId().intValue());//api 的 id
		kkwInsertUser.setSiteName(entity.getProjectAgent());
		kkwInsertUser.setOddtype(oddtype);
		kkwInsertUser.setCurrencyType(cur);
		kkwInsertUser.setCreateTime(DateUtil.getCurrentTime());//创建时间
		kkwInsertUser.setUserStatus(1);//状态=1 有效会员
		kkwInsertUser.setSiteId(entity.getSiteId());
		kkwInsertUser.setSiteName(entity.getProjectAgent());
		this.kkwApiUserEntityMapper.insert(kkwInsertUser);//插入 Ds会员资料
		return kkwInsertUser;
	}
	
	@Override
	public String loginBySingGame(LoginParam param) {
		return null;
	}
}
