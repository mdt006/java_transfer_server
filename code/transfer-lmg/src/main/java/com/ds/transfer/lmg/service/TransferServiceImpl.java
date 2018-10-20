package com.ds.transfer.lmg.service;
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
import com.ds.transfer.lmg.constants.LmgConstants;
import com.ds.transfer.lmg.constants.TelegramConstants;
import com.ds.transfer.lmg.vo.LoginVo;
import com.ds.transfer.lmg.vo.MoneyVo;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.LmgApiUserEntity;
import com.ds.transfer.record.entity.LmgApiUserEntityExample;
import com.ds.transfer.record.entity.TransferRecordEntity;
import com.ds.transfer.record.mapper.LmgApiUserEntityMapper;
import com.ds.transfer.record.service.TransferRecordService;

/**
* @ClassName: TransferServiceImpl
* @Description: TODO(LMG业务接口实现类)
* @author leo
* @date 2018年2月27日
*/
public class TransferServiceImpl extends CommonTransferService implements TransferService<LmgApiUserEntity> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;
	@Resource
	private LmgApiUserEntityMapper lmgApiUserEntityMapper;
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
		try {
			String url = entity.getReportUrl().split(",")[1];
			if(!StringsUtil.isNull(entity.getPrefix())){
				prefix = entity.getPrefix();
			}
			String platformUser = prefix.concat(transferParam.getUsername());
			
			message.append("LMG Transfer 站点：").append(entity.getSiteId());
			message.append("会员：").append(platformUser);
			message.append(",转账：").append(transferParam.getType().equals("IN")==true ? "DS-->LMG" : "LMG-->DS");
			message.append(",单号:").append(transferParam.getBillno());
			message.append(",金额：").append(transferParam.getCredit()).append("\n");
			
			//插入lmg转账记录
			dsRecord = this.transferRecordService.insert(entity.getSiteId(), entity.getLiveId(), transferParam.getTransRecordId(), entity.getPassword(),
					transferParam.getUsername(), transferParam.getCredit(), transferParam.getBillno(),transferParam.getType(), LmgConstants.LMG, transferParam.getRemark(), dsRecord);
			logger.info("lmg 转账记录插入成功,username={},siteId={},id = {}",transferParam.getUsername(),entity.getSiteId(),dsRecord.getId());
			MoneyVo vo = new MoneyVo(platformUser, entity.getPassword(), transferParam.getBillno(),"ds钱包转" + (IN.equals(transferParam.getType()) ? "入" : "出"), transferParam.getCredit());
			String paramBody = JSONUtils.bean2Json(vo);
			
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("hashCode", entity.getHashcode());
			param.put("command", (IN.equals(transferParam.getType()) ? LmgConstants.IN : LmgConstants.OUT));
			param.put("params", JSONObject.parse(paramBody));
			message.append("请求地址：").append(url).append("\n");
			message.append("请求参数").append(JSONUtils.map2Json(param)).append("\n");
			logger.info("lmg transfer before username={},siteId={},url ={},param={}",transferParam.getUsername(),entity.getSiteId(),url,param.toString());
			
			long startTime = System.currentTimeMillis();
			String result = StringsUtil.sendPost1(url, JSONUtils.map2Json(param));
			long endTime = System.currentTimeMillis();
			message.append("接口返回：").append(result).append("\n");
			logger.info("lmg transfer after username={},siteId={},elapsedTime={} ms,result={}",transferParam.getUsername(),entity.getSiteId(),(endTime-startTime),result);
			
			JSONObject resultObj = JSONObject.parseObject(result);
			if ("0".equals(resultObj.getString("errorCode"))) {
				logger.info("lmg transfer success username={},siteId={},type= {}",transferParam.getUsername(),entity.getSiteId(),transferParam.getType().equals("IN")==true?"DS->LMG":"LMG->DS");
				dsRecord = this.transferRecordService.update(SysConstants.Record.TRANS_SUCCESS, "LMG转账成功", dsRecord);
				return JSONUtils.map2Json(success("LMG转账成功"));
			}
			logger.info("lmg transfer after username={},siteId={} transfer error!",transferParam.getUsername(),entity.getSiteId());
			dsRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "lmg转账失败", dsRecord);
			
			message.insert("LMG Transfer".length(),"转账异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-lmg transfer",message.toString().replace("&","*"));
			return JSONUtils.map2Json(failure("LMG转账失败"));
		} catch (Exception e) {
			dsRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "lmg转账异常", dsRecord);
			logger.info("lmg transfer username={},siteId={} transfer error：={}!",transferParam.getUsername(),entity.getSiteId(),e);
			e.printStackTrace();
			message.insert("LMG Transfer".length(),"转账异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-lmg transfer",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("system error !" + msg));
		}
	}
	
	@Override
	public String queryBalance(QueryBalanceParam param) {
		StringBuilder message = new StringBuilder();
		String prefix = "";
		try {
			ApiInfoEntity entity = param.getEntity();
			if(!StringsUtil.isNull(entity.getPrefix())){
				prefix = entity.getPrefix();
			}
			String platformUser= prefix.concat(param.getUsername());
			
			message.append("LMG qeuryBalance 站点：").append(entity.getSiteId());
			message.append(",会员：").append(platformUser+",\n");
			
			Map<String, Object> lmgParam = new HashMap<>();
			lmgParam.put("hashCode", entity.getHashcode());
			lmgParam.put("command",LmgConstants.BALANCE);
			String url = entity.getReportUrl().split(",")[1];
			Map<String, Object> paramBody = new HashMap<>();
			paramBody.put("username",platformUser);
			paramBody.put("password", entity.getPassword());
			lmgParam.put("params", paramBody);
			String sendParam = JSONUtils.map2Json(lmgParam);
			message.append("请求地址：").append(url).append("\n");
			message.append("请求参数：").append(sendParam).append("\n");
			logger.info("lmg queryBalance before username={},siteId={},param={}",param.getUsername(),entity.getSiteId(),sendParam);
			
			long startTime = System.currentTimeMillis();
			String result = StringsUtil.sendPost1(url, sendParam);
			long endTime = System.currentTimeMillis();
			logger.info("lmg queryBalance after username={},siteId={},elapsedTime={} ms,result={}",param.getUsername(),entity.getSiteId(),(endTime-startTime),result);
			
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
			
			message.insert("LMG qeuryBalance".length(),"查询余额异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-lmg qeuryBalance",message.toString().replace("&","*"));
			return JSONUtils.map2Json(failure(result));
		} catch (Exception e) {
			message.insert("LMG qeuryBalance".length(),"查询余额异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-lmg qeuryBalance",message.toString().replace("&","*"));
			logger.error("lmg qeuryBalance username={},siteId={},error:={}!",param.getUsername(),param.getEntity().getSiteId(),e);
			e.printStackTrace();
			return JSONUtils.map2Json(maybe("查询余额失败"));
		}
	}

	@Override
	public String login(LoginParam param) {
		StringBuilder message = new StringBuilder();
		String tipStatus = "CLOSE"; //小费关闭状态
		String result = null;
		String prefix = "";
		ApiInfoEntity entity = param.getEntity();
		if(!StringsUtil.isNull(entity.getPrefix())){
			prefix = entity.getPrefix();
		}
		String platformUser = prefix.concat(param.getUsername());
		message.append("LMG login 站点：").append(entity.getSiteId());
		message.append(",会员：").append(param.getUsername()).append("\n");
		try {
			String gameType = param.getGameType();
			LoginVo vo = new LoginVo(entity.getHashcode(),LmgConstants.LOGIN);
			LoginVo.Param params = vo.new Param(platformUser, StringsUtil.toMD5(entity.getPassword()), StringsUtil.isNull(param.getCur()) ? entity.getCurrencyType() : param.getCur(),
					platformUser, param.getLanguage(), Integer.valueOf(param.getLine()),gameType,tipStatus);
			vo.setParams(params);
			String loginParam = JSONUtils.bean2Json(vo);
			String url = entity.getReportUrl().split(",")[0];
			logger.info("lmg login before username={},siteId={},url={},param={}",param.getUsername(),entity.getSiteId(),url,loginParam);
			message.append("请求地址：").append(url).append("\n");
			message.append("请求参数：").append(loginParam).append("\n");
			long startTime = System.currentTimeMillis();
				result = StringsUtil.sendPost1(url, loginParam);
			long endTime = System.currentTimeMillis();
			message.append("接口返回：").append(result).append("\n");
			logger.info("lmg login after username={},siteId={},elapsedTime={} ms,result={}",param.getUsername(),entity.getSiteId(),(endTime-startTime),result);
			JSONObject jsonMap = JSONObject.parseObject(result);
			if ("0".equals(jsonMap.getString("errorCode"))) {
				result = jsonMap.getJSONObject("params").getString("link");
				return JSONUtils.map2Json(success(result));
			}
			
			if("6606".equals(jsonMap.getString("errorCode"))){
				Thread.sleep(2000);
				logger.info("username={},siteId={},密码不正确，开始重置密码！",platformUser,entity.getSiteId());
				Map<String,Object> paramsMap = new HashMap<String,Object>();
				paramsMap.put("username",platformUser);
				paramsMap.put("password",entity.getPassword());
				Map<String,Object> paramMap = new HashMap<String,Object>();
				paramMap.put("hashCode",entity.getHashcode());
				paramMap.put("command", "CHANGE_PASSWORD");
				paramMap.put("params", paramsMap);
				logger.info("lmg before chancePassword username={},siteId={}, param={}",platformUser,entity.getSiteId(),loginParam);
				String chancePasswordResult = StringsUtil.sendPost1(url, JSONUtils.map2Json(paramMap));
				JSONObject jsonObject = JSONObject.parseObject(chancePasswordResult);
				logger.info("lmg after chancePassword username={},siteId={}, result={}",platformUser,entity.getSiteId(),chancePasswordResult);
				if(jsonObject.containsKey("errorCode") && 0 == jsonObject.getInteger("errorCode")){
					Thread.sleep(2000);
					logger.info("username={},siteId={},重置密码成功！",platformUser,entity.getSiteId());
					this.login(param);
				}
			}
			message.insert("LMG login".length(),"登录异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-lmg login",message.toString().replace("&", "*"));
		} catch (Exception e) {
			message.insert("LMG login".length(),"登录异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-lmg login",message.toString().replace("&", "*"));
			logger.error("lmg login username={},siteId={}, error:={}!",param.getUsername(),entity.getSiteId(),e);
			e.printStackTrace();
			return JSONUtils.map2Json(maybe("system error : " + result));
		}
		return JSONUtils.map2Json(failure(result));
	}
	
	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		StringBuilder message = new StringBuilder();
		ApiInfoEntity entity = param.getEntity();
		
		message.append("LMG queryStatusByBillno 站点：").append(entity.getSiteId());
		message.append(",会员：").append(param.getUsername());
		
		try {
			String url = entity.getReportUrl().split(",")[1];
			Map<String, Object> lmgParam = new HashMap<>();
			lmgParam.put("hashCode", entity.getHashcode());
			lmgParam.put("command",LmgConstants.CHECK);
			
			Map<String, Object> paramBody = new HashMap<>();
			paramBody.put("ref", param.getBillno());
			lmgParam.put("params", paramBody);
			String sendParam = JSONUtils.map2Json(lmgParam);
			message.append("请求地址：").append(url).append("\n");
			message.append("请求参数：").append(sendParam).append("\n");
			logger.info("lmg queryStatusByBillno before username={},siteId={},url={},param={}",param.getUsername(),entity.getSiteId(),url,sendParam);
			long startTime = System.currentTimeMillis();
			String result = StringsUtil.sendPost1(url,sendParam);
			long endTime = System.currentTimeMillis();
			message.append("接口返回：").append(result).append("\n");
			logger.info("lmg queryStatusByBillno after username={},siteId={},elapsedTime={} ms,result={}",param.getUsername(),entity.getSiteId(),(endTime-startTime),result);
			
			JSONObject resultObj = JSONObject.parseObject(result);
			if ("6601".equals(resultObj.getString("errorCode"))) {
				return JSONUtils.map2Json(success("success!"));
			} else if ("6617".equals(resultObj.getString("errorCode"))) {
				message.insert("LMG queryStatusByBillno".length(),"查询订单异常！").append("\n");
				telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-lmg queryStatusByBillno",message.toString().replace("&", "*"));
				return JSONUtils.map2Json(maybe("billno = " + param.getBillno() + " is processing!"));
			} else {
				return JSONUtils.map2Json(failure(result));
			}
		} catch (Exception e) {
			message.insert("LMG queryStatusByBillno".length(),"查询订单异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-lmg queryStatusByBillno",message.toString().replace("&", "*"));
			logger.info("lmg queryStatusByBillno username={},siteId={},url={},param={} error:{}!",param.getUsername(),entity.getSiteId(),e);
			e.printStackTrace();
			return JSONUtils.map2Json(maybe("system error!"));
		}
	}
	
	@Override
	public String queryAgentBalance(QueryBalanceParam param) {
		logger.info("lmg queryAgentBalance username={},siteId={},agent={}",param.getUsername(),param.getEntity().getSiteId(),param.getAgent());
		Map<String, Object> resultMap = null;
		double balance = 0;  
		try {
			String siteId = String.valueOf(param.getEntity().getSiteId());
			logger.info("lmg 查询redis缓存代理总额度 agent{}, siteId{}",param.getAgent(),siteId);
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
			logger.info("lmg queryAgentBalance username={},siteId={},agent={},totalBalance={}",param.getUsername(),param.getEntity().getSiteId(),param.getAgent(),balance);
			return JSONUtils.map2Json(resultMap);
		} catch (Exception e) {
			logger.info("lmg queryAgentBalance username={},siteId={},agent={},error:{}!",param.getUsername(),param.getEntity().getSiteId(),param.getAgent(),e);
			e.printStackTrace();
			resultMap = maybe("查询余额异常");
		}
		return JSONUtils.map2Json(resultMap);
	}
	
	@Override
	@Transactional
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		logger.info("lmg checkAndCreateMember username={},siteId={}",param.getUsername(),param.getEntity().getSiteId());
		Map<String, Object> resultMap = new HashMap<String,Object>();
		Integer siteId = null;
		String prefix = "";
		ApiInfoEntity entity = param.getEntity();
		String password = entity.getPassword();
		String username = param.getUsername();
		if(!StringsUtil.isNull(entity.getPrefix())){
			prefix = entity.getPrefix();
		}
		String platformUser = prefix.concat(username);
		String siteIdUser = String.valueOf(entity.getSiteId()).concat("|").concat(platformUser);
		String cur = "";
		try {
			cur = StringsUtil.isNull(param.getCur()) ? entity.getCurrencyType() : param.getCur();
			String oddType = StringsUtil.isNull(param.getOddtype()) ? SysConstants.CUR : param.getOddtype();
			siteId = entity.getSiteId();
			//查询本地是否存在该用户
			LmgApiUserEntity user = this.queryUserExist(siteIdUser);
			
			logger.info("lmg checkAndCreateMember username={},siteId={},会员-->{}",username,siteId,user==null?"本地不存在,走远程创建会员":"本地已存在");
			if (user == null) {
				//1.创建LMG会员
				LoginParam loginParam = new LoginParam(entity,username);
				loginParam.setLine("0"); //默认是用线路0
				loginParam.setGameType(LmgConstants.GAME_TYPE);
				loginParam.setCur(cur);
				logger.info("lmg network create user before username={},siteId={}",username,siteId);
				resultMap = JSONUtils.json2Map(this.login(loginParam));
				logger.info("lmg network create user after username={},siteId={}",username,siteId,resultMap.toString());
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					//2.创建本地会员
					user = this.createMemberByLocal(entity,platformUser,password,cur,oddType);
				}
				return resultMap;
			}
		} catch (Exception e) {
			logger.info("lmg network create user username={},siteId={},error:{}!",username,String.valueOf(siteId),e);
			e.printStackTrace();
			return maybe("创建LMG会员异常");
		}
		return success("用户已经存在");
	}
	
	@Override
	public LmgApiUserEntity queryUserExist(String siteIdUser) {
		
		String[] siteUser = siteIdUser.split("\\|");
		LmgApiUserEntityExample lmgApiUserExample = new LmgApiUserEntityExample();
		lmgApiUserExample.createCriteria().andUsernameEqualTo(siteUser[1]).andSiteIdEqualTo(Integer.parseInt(siteUser[0]));
		List<LmgApiUserEntity> list = this.lmgApiUserEntityMapper.selectByExample(lmgApiUserExample);
		logger.info("list={}",list.toString());
		return (list == null || list.size() == 0) ? null : list.get(0);
	}
	
	/**
	 * 本地创建会员
	 */
	private LmgApiUserEntity createMemberByLocal(ApiInfoEntity entity, String username, String password, String cur, String oddtype) {
		LmgApiUserEntity lmgInsertUser = new LmgApiUserEntity();
		lmgInsertUser.setUsername(username);
		lmgInsertUser.setPassword(password);
		lmgInsertUser.setAgentName(entity.getAgent());
		//lmgInsertUser.setApiInfoId(entity.getId().intValue());//api 的 id
		lmgInsertUser.setSiteName(entity.getProjectAgent());
		lmgInsertUser.setOddtype(oddtype);
		lmgInsertUser.setCurrencyType(cur);
		lmgInsertUser.setCreateTime(DateUtil.getCurrentTime());//创建时间
		lmgInsertUser.setUserStatus(1);//状态=1 有效会员
		lmgInsertUser.setSiteId(entity.getSiteId());
		lmgInsertUser.setSiteName(entity.getProjectAgent());
		this.lmgApiUserEntityMapper.insert(lmgInsertUser);//插入 LMG会员资料
		return lmgInsertUser;
	}
	
	@Override
	public String loginBySingGame(LoginParam param) {
		return null;
	}
}
