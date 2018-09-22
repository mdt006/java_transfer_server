package com.ds.transfer.sgs.service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.BetLimit;
import com.ds.transfer.record.entity.BetLimitExample;
import com.ds.transfer.record.entity.SgsApiUserEntity;
import com.ds.transfer.record.entity.SgsApiUserEntityExample;
import com.ds.transfer.record.entity.TransferRecordEntity;
import com.ds.transfer.record.mapper.BetLimitMapper;
import com.ds.transfer.record.mapper.SgsApiUserEntityMapper;
import com.ds.transfer.record.service.TransferRecordService;
import com.ds.transfer.sgs.constants.SgsConstants;
import com.ds.transfer.sgs.constants.TelegramConstants;
import com.ds.transfer.sgs.utils.DateUtils;
import com.ds.transfer.sgs.utils.RequestUtils;
import com.ds.transfer.sgs.utils.SHA1Utils;
import com.ds.transfer.sgs.vo.TransferHistory;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;

/**
* @ClassName: TransferServiceImpl
* @Description: TODO(SGS业务接口实现类)
* @author leo
* @date 2018年6月06日
*/
public class TransferServiceImpl extends CommonTransferService implements TransferService<SgsApiUserEntity> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;
	@Resource
	private SgsApiUserEntityMapper sgsApiUserEntityMapper;
	@Autowired
	protected RedisTemplate<String, String> redisTemplate;
	@Autowired
	private BetLimitMapper betLimitMapper;
	
	public static final String TOKEN = "sgs_token";
	
	//小飞机消息组件
	TelegramMessage telegramMessage = TelegramMessage.getInstance();
		
	@Override
	public String transfer(TransferParam transferParam) {
		TransferRecordEntity dsRecord = new TransferRecordEntity();
		StringBuilder message = new StringBuilder();
		ApiInfoEntity entity = null;
		String msg = null;
		String result = "";
		String prefix = "";
		String url = "";
		String platformUser = "";
		
		try {
			entity = transferParam.getEntity();
			url = entity.getReportUrl().split(",")[0];
			if(!StringsUtil.isNull(entity.getPrefix())){
				prefix = entity.getPrefix();
			}
			platformUser = prefix.concat(transferParam.getUsername());
			
			message.append("SGS Transfer 站点：").append(entity.getSiteId());
			message.append(",会员：").append(transferParam.getUsername());
			message.append(",转账：").append(transferParam.getType().equals("IN")==true?"DS-->SGS":"SGS-->DS");
			message.append(",单号:").append(transferParam.getBillno());
			message.append(",金额：").append(transferParam.getCredit()).append("\n");
			
			String cur = "CNY".equals(transferParam.getCur()) ? SysConstants.SGS_CUR : transferParam.getCur();
			String api = IN.equals(transferParam.getType()) ? SgsConstants.IN : SgsConstants.OUT;
			url = url.concat(api);
			//插入sgs转账记录
			dsRecord = this.transferRecordService.insert(entity.getSiteId(), entity.getLiveId(),
					transferParam.getTransRecordId(), entity.getPassword(),
					transferParam.getUsername(), transferParam.getCredit(), transferParam.getBillno(),
					transferParam.getType(), SgsConstants.SGS, transferParam.getRemark(), dsRecord);
			logger.info("sgs 转账记录插入成功,username={},siteId={},id = {}",platformUser,entity.getSiteId(),dsRecord.getId());

			//toSign拼接进行加密
			String utcTime = DateUtils.getUTCTime();
			StringBuilder StringToSign = new StringBuilder();
			StringToSign.append(entity.getSecret()).append(utcTime);
			//HmacSHAl加密
			String signature = SHA1Utils.encrypt(entity.getSecret(),StringToSign.toString());
			
			//封装请求参数及请求头信息
			Map<String,Object> requestParam = new HashMap<String,Object>();
			requestParam.put("userid",platformUser);
			requestParam.put("amt",transferParam.getCredit());
			requestParam.put("cur",cur);
			requestParam.put("txid",transferParam.getBillno());
			requestParam.put("timestamp",DateUtils.getUTCTimeFormat());
			requestParam.put("desc",null);
			
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("signature", signature);
			param.put("sgsDate", utcTime);
			param.put("requestParam", JSONObject.toJSON(requestParam));
			message.append("请求地址：").append(url).append("\n");
			message.append("请求参数：").append(param.toString()).append("\n");
			logger.info("sgs transfer before username={},siteId={},url ={},param={}",platformUser,entity.getSiteId(),url,param.toString());
			long startTime = System.currentTimeMillis();
			result = RequestUtils.sendPost(url,param,"utf-8");
			long endTime = System.currentTimeMillis();
			message.append("接口返回：").append(result).append("\n");
			logger.info("sgs transfer after username={},siteId={},elapsedTime={} ms,result={}",platformUser,entity.getSiteId(),(endTime-startTime),result);
			
			JSONObject resultObj = JSONObject.parseObject(result);
			//判断是否返回异常状态码，是否重复
			if (StringsUtil.isNull(resultObj.getString("err")) && resultObj.getBooleanValue("dup") == false) {
				logger.info("sgs transfer success username={},siteId={},type= {}",platformUser,entity.getSiteId(),transferParam.getType().equals("IN")==true?"DS->SGS":"SGS->DS");
				dsRecord = this.transferRecordService.update(SysConstants.Record.TRANS_SUCCESS, "SUN转账成功", dsRecord);
				return JSONUtils.map2Json(success("sgs转账成功"));
			}
			logger.info("sgs transfer after username={},siteId={} transfer error!",platformUser,entity.getSiteId());
			dsRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "SUN转账失败", dsRecord);
			
			message.insert("SGS Transfer".length(),"转账异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-sgs transfer",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("SGS转账失败"));
		} catch (Exception e) {
			logger.info("sgs transfer username={},siteId={} transfer error：={}!",transferParam.getUsername(),entity.getSiteId(),e);
			dsRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "SUN转账异常", dsRecord);
			message.insert("SGS Transfer".length(),"转账异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-sgs transfer",message.toString().replace("&","*"));
			e.printStackTrace();
			return JSONUtils.map2Json(maybe("system error !" + msg));
		}
	}
	
	@Override
	public String queryBalance(QueryBalanceParam param) {
		StringBuilder message = new StringBuilder();
		ApiInfoEntity entity = null;
		String prefix = "";
		String platformUser = "";
		String result = "";
		
		try {
			entity = param.getEntity();
			if(!StringsUtil.isNull(entity.getPrefix())){
				prefix = entity.getPrefix();
			}
			StringBuilder url = new StringBuilder(entity.getReportUrl().split(",")[0]);
			platformUser = prefix.concat(param.getUsername());
			message.append("SGS qeuryBalance 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			//toSign拼接进行加密
			String utcTime = DateUtils.getUTCTime();
			StringBuilder StringToSign = new StringBuilder();
			StringToSign.append(entity.getSecret()).append(utcTime);
			
			//HmacSHAl加密
			String signature = SHA1Utils.encrypt(entity.getSecret(),StringToSign.toString());
			
			//封装请求参数
			Map<String,Object> requestParam = new HashMap<String,Object>();
			requestParam.put("signature", signature);
			requestParam.put("sgsDate",utcTime);
			url.append(SgsConstants.BALANCE);
			url.append("?userid=").append(platformUser);
			url.append("&cur=").append(param.getCur());
			message.append("请求地址：").append(url).append("\n");
			message.append("请求参数：").append(requestParam).append("\n");
			logger.info("sgs queryBalance before username={},siteId={},param={}",platformUser,entity.getSiteId(),url.toString());
			long startTime = System.currentTimeMillis();
			result = RequestUtils.sendGet(url.toString(), requestParam);
			long endTime = System.currentTimeMillis();
			message.append("接口返回:").append(result).append("\n");
			logger.info("sgs queryBalance after username={},siteId={},elapsedTime={} ms,result={}",platformUser,entity.getSiteId(),(endTime-startTime),result);
			
			JSONObject resultObj = JSONObject.parseObject(result);
			if (StringsUtil.isNull(resultObj.getString("err"))) {
				String balance = resultObj.getString("bal");
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
			message.insert("SGS qeuryBalance".length(),"查询余额异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-sgs queryBalance",message.toString().replace("&","*"));
			return JSONUtils.map2Json(failure(result));
		} catch (Exception e) {
			logger.error("sgs qeuryBalance username={},siteId={},error:={}!",platformUser,param.getEntity().getSiteId(),e);
			TelegramMessage telegramMessage = TelegramMessage.getInstance();
			message.insert("SGS qeuryBalance".length(),"查询余额异常！").append("\n");
			message.append("异常信息：").append(e.toString());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-sgs qeuryBalance",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("查询余额失败"));
		}
	}

	@Override
	public String login(LoginParam param) {
		StringBuilder message = new StringBuilder();
		StringBuilder result = new StringBuilder();
		String prefix = "";
		String token = "";
		String platformUser = "";
		
		ApiInfoEntity entity = param.getEntity();
		if(!StringsUtil.isNull(entity.getPrefix())){
			prefix = entity.getPrefix();
		}
		try {
			platformUser = prefix.concat(param.getUsername());
			//终端 0代表 PC端，1代表手机端
			String terminal = "PC".equals(param.getTerminal()) == true ? "0" :"1";
			String url = entity.getReportUrl().split(",")[1];
			
			message.append("SGS login 站点：").append(param.getEntity().getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			String redisKey = TOKEN+"_"+entity.getLiveId()+"_"+entity.getSiteId()+"_"+platformUser;
	        token = this.redisTemplate.opsForValue().get(redisKey);
	        
	        if(StringsUtil.isNull(token)){
	        	UserParam userParam = new UserParam();
	        	userParam.setEntity(entity);
	        	userParam.setIsDemo(param.getIsDemo());
	        	userParam.setTerminal(param.getTerminal());
	        	userParam.setPlayerIp(param.getPlayerIp());
	        	userParam.setUsername(param.getUsername());
	        	token=this.getToken(userParam);
	        }
	        result.append(url).append(SgsConstants.LOGIN).append("?");
        	result.append("gpcode=").append(param.getGameType());
        	result.append("&gcode=").append(param.getGamecode());
        	result.append("&platform=").append(terminal);
        	result.append("&token=").append(token);
        	message.append("返回报文：").append(result.toString()).append("\n");
        	logger.info("sgs login after username={},siteId={},result={}",platformUser,entity.getSiteId(),result.toString());
        	if(StringsUtil.isNull(token)){
        		return JSONUtils.map2Json(failure("登录异常！"));
        	}
        	return JSONUtils.map2Json(success(result.toString()));
		} catch (Exception e) {
			logger.error("sgs login username={},siteId={}, error:={}!",param.getUsername(),entity.getSiteId(),e);
			e.printStackTrace();
			message.insert("SGS login".length(), "登陆异常！").append("\n");
			message.append("异常信息：").append(e.toString());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-sgs login",message.toString().replace("&", "*"));
			return JSONUtils.map2Json(maybe("system error : " + result.toString()));
		}
	}
	
	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		StringBuilder message = new StringBuilder();
		ApiInfoEntity entity = null;
		Reader reader = null;
		String prefix = "";
		String result = "";
		try {
			 entity = param.getEntity();
			if(!StringsUtil.isNull(entity.getPrefix())){
				prefix = entity.getPrefix();
			}
			String platformUser = prefix.concat(param.getUsername());
			
			message.append("SGS queryStatusByBillno 站点：").append(entity.getSiteId());
			message.append(",会员：").append(platformUser);
			
			StringBuilder url = new StringBuilder(entity.getReportUrl().split(",")[0]);
			//toSign拼接进行加密
			String utcTime = DateUtils.getUTCTime();
			StringBuilder StringToSign = new StringBuilder();
			StringToSign.append(entity.getSecret()).append(utcTime);
			//HmacSHAl加密
			String signature = SHA1Utils.encrypt(entity.getSecret(),StringToSign.toString());
			
			//封装请求参数
			Map<String,Object> requestParam = new HashMap<String,Object>();
			requestParam.put("signature", signature);
			requestParam.put("sgsDate",utcTime);
			logger.info("signature={},sgsDate={}",signature,utcTime);
			url.append(SgsConstants.CHECK);
			url.append("?userid=").append(platformUser);
			url.append("&includetestplayers=").append(true);
			url.append("&startdate=").append(DateUtils.getFromNow(-1).replace("+", "%2B"));
			url.append("&enddate=").append(DateUtils.getUTCTime().replace("Z", "%2B00:00"));
			message.append("请求报文：").append(url.toString()).append("\n");
			
			logger.info("sgs queryStatusByBillno before username={},siteId={},url={}",platformUser,entity.getSiteId(),url);
			long startTime = System.currentTimeMillis();
			result = RequestUtils.sendGet(url.toString(),requestParam);
			long endTime = System.currentTimeMillis();
			message.append("接口返回：").append(result).append("\n");
			logger.info("sgs queryStatusByBillno after username={},siteId={},elapsedTime={} ms,result={}",platformUser,entity.getSiteId(),(endTime-startTime),result);
			
			reader = new InputStreamReader(new ByteArrayInputStream(result.getBytes("UTF-8"))); 
            @SuppressWarnings("deprecation")
			CSVReader csvReader = new CSVReader(reader, CSVWriter.DEFAULT_SEPARATOR,CSVWriter.NO_QUOTE_CHARACTER);
            HeaderColumnNameMappingStrategy<TransferHistory> strategy = new HeaderColumnNameMappingStrategy<TransferHistory>();  
            strategy.setType(TransferHistory.class);

            CsvToBean<TransferHistory> csvToBean = new CsvToBean<TransferHistory>();
            @SuppressWarnings("deprecation")
			List<TransferHistory> historyList = csvToBean.parse(strategy, csvReader);
            for (TransferHistory transferHistory : historyList) {
				String txid = transferHistory.getTxid();
				String userId = transferHistory.getUserid();
				if(param.getBillno().equals(txid) && platformUser.equals(userId)){
					return JSONUtils.map2Json(success("success!"));
				}
			}
            message.insert("SGS queryStatusByBillno".length(),"查询订单不存在！");
            telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-sgs queryStatusByBillno",message.toString().replace("&","*"));
			return JSONUtils.map2Json(failure(result));
		} catch (Exception e) {
			logger.info("sgs queryStatusByBillno username={},siteId={},url={},param={} error:{}!",param.getUsername(),entity.getSiteId(),e);
			e.printStackTrace();
			message.insert("SGS queryStatusByBillno".length(),"查询订单不存在！");
            telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-sgs queryStatusByBillno",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("查询订单状态出错 : " + result));
		}finally{
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public String queryAgentBalance(QueryBalanceParam param) {
		logger.info("sgs queryAgentBalance username={},siteId={},agent={}",param.getUsername(),param.getEntity().getSiteId(),param.getAgent());
		Map<String, Object> resultMap = null;
		ApiInfoEntity entity = null;
		double balance = 0;  
		try {
			entity = param.getEntity();
			String siteId = String.valueOf(entity.getSiteId());
			String liveId = String.valueOf(entity.getLiveId());
			
			logger.info("sgs 查询redis缓存代理总额度 siteId{}",siteId);
			String redisKey = liveId+"_"+siteId+"_*";
			Set<String> keys = this.redisTemplate.keys(redisKey);
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
			logger.info("sgs queryAgentBalance username={},siteId={},agent={},totalBalance={}",param.getUsername(),entity.getSiteId(),param.getAgent(),balance);
			return JSONUtils.map2Json(resultMap);
		} catch (Exception e) {
			logger.info("sgs queryAgentBalance username={},siteId={},agent={},error:{}!",param.getUsername(),entity.getSiteId(),param.getAgent(),e);
			e.printStackTrace();
			resultMap = maybe("查询活动会员余额异常");
		}
		return JSONUtils.map2Json(resultMap);
	}
	
	@Override
	@Transactional
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		String prefix = "";
		String platformUser = "";
		ApiInfoEntity entity = param.getEntity();
		
		try {
			String password = entity.getPassword();
			String cur = StringsUtil.isNull(param.getCur()) ? entity.getCurrencyType() : param.getCur();
			if(!StringsUtil.isNull(entity.getPrefix())){
				prefix = entity.getPrefix();
			}
			
			platformUser = prefix.concat(param.getUsername());
			String siteIdUser = String.valueOf(entity.getSiteId()).concat("|").concat(platformUser);
			
			SgsApiUserEntity user = this.queryUserExist(siteIdUser);
			if (user == null) {
				String oddType = querySgsOddtype(param);
				//1、远程获取token
				String token = this.getToken(param);
				if(!StringsUtil.isNull(token)){
					//2.创建本地会员
					this.createMemberByLocal(entity,platformUser,password,cur,oddType);
					return success("创建用户成功！");
				}else{
					logger.error("sgs username={},siteId={}, 获取token失败！",platformUser,entity.getSiteId());
				}
			}
		}catch (Exception e) {
			logger.info("sgs createLocateUser username={},siteId={},error:{}!",platformUser,String.valueOf(entity.getSiteId()),e);
			e.printStackTrace();
			return maybe("创建用户异常");
		}
		return success("用户已经存在");
	}
	
	// 获取Token
	private String getToken(UserParam param) throws Exception {
		StringBuilder message = new StringBuilder();
		String token = "";
		String prefix = "";
		try {
			ApiInfoEntity entity = param.getEntity();
			Integer siteId = entity.getSiteId();
			String username = param.getUsername();
			message.append("SGS getToken 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			//客户端没传ip默认厦门VPN
			String playerIp = StringsUtil.isNull(param.getPlayerIp()) ? "120.24.84.13" : param.getPlayerIp();
			String url = entity.getReportUrl().split(",")[0].concat(SgsConstants.AUTHORIZE);
			//是否是试玩 false 代表 正式，true 代表试玩
			boolean isDemo = "1".equals(param.getIsDemo())? false : true;
			//终端 0代表 PC端，1代表手机端
			String terminal = "PC".equals(param.getTerminal()) == true ? "0" :"1";
			String cur = StringsUtil.isNull(param.getCur()) ? entity.getCurrencyType() : param.getCur();
			
			if(!StringsUtil.isNull(entity.getPrefix())){
				prefix = entity.getPrefix();
			}
			String platformUser = prefix.concat(username);
			// 生成请求头信息
			String UTCTimeStr = DateUtils.getUTCTime();
			StringBuilder StringToSign = new StringBuilder();
			StringToSign.append(entity.getSecret()).append(UTCTimeStr);
			String signature = SHA1Utils.encrypt(entity.getSecret(),StringToSign.toString());
			// 查询盘口配置
			String oddType = querySgsOddtype(param);
			// 封装请求报文
			Map<String, Object> requestParam = new HashMap<String, Object>();
			requestParam.put("ipaddress", playerIp);
			requestParam.put("username", username);
			requestParam.put("userid", platformUser);
			requestParam.put("lang", entity.getLanguage());
			requestParam.put("cur", cur);
			requestParam.put("betlimitid", oddType);
			requestParam.put("platformtype", terminal);
			requestParam.put("istestplayer", isDemo);

			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("signature", signature);
			paramMap.put("sgsDate", UTCTimeStr);
			paramMap.put("requestParam", JSONObject.toJSON(requestParam));
			message.append("请求地址：").append(url).append("\n");
			message.append("请求参数：").append(paramMap.toString()).append("\n");
			logger.info("sgs network createUser getToken before username={},siteId={},url={},param={}",username, siteId, url, paramMap.toString());
			String result = RequestUtils.sendPost(url, paramMap, "utf-8");
			message.append("接口返回：").append(result).append("\n");
			JSONObject resultObj = JSONObject.parseObject(result);
			logger.info("sgs network createUser getToken after username={},siteId={},result={}",username, siteId, result);

			if (StringsUtil.isNull(resultObj.getString("err"))) {
				token = resultObj.getString("authtoken");
				// 将生成的token添加到缓存，用于登录,缓存50分钟自动清除
				String redisKey = TOKEN + "_" + entity.getLiveId() + "_"+ entity.getSiteId() + "_" + platformUser;
				this.redisTemplate.opsForValue().set(redisKey, token);
				this.redisTemplate.expire(redisKey, 50, TimeUnit.MINUTES);
				return token;
			}
			
			message.insert("SGS getToken".length(),"获取token异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-sgs getToken",message.toString().replace("&","*"));
		} catch (Exception e) {
			message.insert("SGS getToken".length(),"获取token异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-sgs getToken",message.toString().replace("&","*"));
			e.printStackTrace();
		}
		return token;
	}

	/**
	 * 查询限红
	 * @param param
	 * @return
	 */
	private String querySgsOddtype(UserParam param){
		String oddType = null;
		String prefix = "";
		String platformUser = "";
		
		try {
			String username = param.getUsername();
			ApiInfoEntity entity = param.getEntity();
			if(!StringsUtil.isNull(entity.getPrefix())){
				prefix = entity.getPrefix();
			}
			platformUser = prefix.concat(username);
			
			BetLimitExample betLimitExample = new BetLimitExample();
			betLimitExample.createCriteria().andSiteIdEqualTo(entity.getSiteId()).andLiveIdEqualTo(String.valueOf(entity.getLiveId()));
			List<BetLimit> betLimitList = betLimitMapper.selectByExample(betLimitExample);
			
			if(null != betLimitList && betLimitList.size() > 0){
				BetLimit betLimit = betLimitList.get(0);
				oddType = StringsUtil.isNull(betLimit.getOddtype()) ? "1": betLimit.getOddtype();
				logger.info("sgs querySgsOddtype after update username={},siteId={}, result = {}", platformUser,entity.getSiteId(),oddType);
			}else{
				oddType = "1";
				logger.info("sgs querySgsOddtype username={},siteId={} 无配置信息,走默认限红！",platformUser,entity.getSiteId());
			}
		}catch(Exception e){
			logger.error("sgs querySgsOddtype username={},siteId={} error={}",platformUser,param.getEntity().getSiteId(),e);
			TelegramMessage telegramMessage = TelegramMessage.getInstance();
			StringBuilder message = new StringBuilder();
			message.append("SGS querySgsOddtype 站点：").append(param.getEntity().getSiteId());
			message.append(",会员：").append(platformUser);
			message.append(",查询限红盘口异常！").append("\n");
			message.append("异常信息：").append(e.toString());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-sgs querySgsOddtype",message.toString().replace("&","*"));
		}
		return oddType;
	}
	
	@Override
	public SgsApiUserEntity queryUserExist(String siteIdUser) {
		String[] siteUser = siteIdUser.split("\\|");
		SgsApiUserEntityExample sgsApiUserExample = new SgsApiUserEntityExample();
		sgsApiUserExample.createCriteria().andUsernameEqualTo(siteUser[1]).andSiteIdEqualTo(Integer.parseInt(siteUser[0]));
		List<SgsApiUserEntity> list = this.sgsApiUserEntityMapper.selectByExample(sgsApiUserExample);
		return (list == null || list.size() == 0) ? null : list.get(0);
	}
	
	/**
	 * 本地创建会员
	 */
	private SgsApiUserEntity createMemberByLocal(ApiInfoEntity entity, String username, String password, String cur, String oddtype) {
		SgsApiUserEntity sgsInsertUser = new SgsApiUserEntity();
		sgsInsertUser.setUsername(username);
		sgsInsertUser.setPassword(password);
		sgsInsertUser.setAgentName(entity.getAgent());
		sgsInsertUser.setApiInfoId(entity.getId().intValue());//api 的 id
		sgsInsertUser.setSiteName(entity.getProjectAgent());
		sgsInsertUser.setOddtype(oddtype);
		sgsInsertUser.setCurrencyType(cur);
		sgsInsertUser.setCreateTime(DateUtil.getCurrentTime());//创建时间
		sgsInsertUser.setUserStatus(1);//状态=1 有效会员
		sgsInsertUser.setSiteId(entity.getSiteId());
		sgsInsertUser.setSiteName(entity.getProjectAgent());
		this.sgsApiUserEntityMapper.insert(sgsInsertUser);//插入 sgs会员资料
		return sgsInsertUser;
	}
	
	@Override
	public String loginBySingGame(LoginParam param) {
		return null;
	}
}
