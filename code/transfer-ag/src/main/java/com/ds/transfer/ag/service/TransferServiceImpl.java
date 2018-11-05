package com.ds.transfer.ag.service;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ds.msg.TelegramMessage;
import com.ds.transfer.ag.constants.AgConstants;
import com.ds.transfer.ag.constants.TelegramConstants;
import com.ds.transfer.ag.util.PHPDESEncrypt;
import com.ds.transfer.ag.vo.AGTransferVo;
import com.ds.transfer.common.constants.SysConstants;
import com.ds.transfer.common.service.CommonTransferService;
import com.ds.transfer.common.service.TransferService;
import com.ds.transfer.common.util.BigDemicalUtil;
import com.ds.transfer.common.util.DateUtil;
import com.ds.transfer.common.util.JSONUtils;
import com.ds.transfer.common.util.ReflectUtil;
import com.ds.transfer.common.util.StringsUtil;
import com.ds.transfer.common.util.XmlUtil;
import com.ds.transfer.common.vo.LoginParam;
import com.ds.transfer.common.vo.QueryBalanceParam;
import com.ds.transfer.common.vo.QueryOrderStatusParam;
import com.ds.transfer.common.vo.TransferParam;
import com.ds.transfer.common.vo.UserParam;
import com.ds.transfer.record.entity.AgApiUserEntity;
import com.ds.transfer.record.entity.AgApiUserEntityExample;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.TransferRecordEntity;
import com.ds.transfer.record.mapper.AgApiUserEntityMapper;
import com.ds.transfer.record.service.TransferRecordService;

@Service
public class TransferServiceImpl extends CommonTransferService implements TransferService<AgApiUserEntity> {
	private static final Logger logger = LoggerFactory.getLogger(TransferServiceImpl.class);

	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;

	@Resource(name = "moneyCenterService")
	private TransferService<?> moneyCenterService;

	@Autowired
	private AgApiUserEntityMapper agApiUserEntityMapper;
	
	@Autowired
	protected RedisTemplate<String, String> redisTemplate;
	
	//小飞机消息组件
	TelegramMessage telegramMessage = TelegramMessage.getInstance();

	@Override
	public String transfer(TransferParam transferParam) {
		TransferRecordEntity agRecord = new TransferRecordEntity();
		StringBuilder message = new StringBuilder();
		String msg = "";
		
		try {
			ApiInfoEntity entity = transferParam.getEntity();
			String platformUser = entity.getPrefix().concat(transferParam.getUsername());
			String agent = entity.getAgent();//代理名称
			
			message.append("AG Transfer 站点：").append(entity.getSiteId());
			message.append(",会员：").append(transferParam.getUsername());
			message.append(",转账：").append(transferParam.getType().equals("IN")==true ? "DS-->AG":"AG-->DS");
			message.append(",单号:").append(transferParam.getBillno());
			message.append(",金额：").append(transferParam.getCredit()).append("\n");
			
			//ag转账记录
			agRecord = this.transferRecordService.insert(entity.getSiteId(), entity.getLiveId(), transferParam.getTransRecordId(), entity.getPassword(), transferParam.getUsername(), transferParam.getCredit(), transferParam.getBillno(),//
					transferParam.getType(), AgConstants.AG, transferParam.getRemark(), agRecord);
			
			logger.info("ag转账记录插入成功,  id = {}", agRecord.getId());
			int actype = SysConstants.TRY_PLAY.equals(entity.getIsDemo() + "") ? 0 : 1;
			BigDecimal remit = new BigDecimal(transferParam.getCredit()).setScale(2, BigDecimal.ROUND_DOWN);
			//ag预转账
			AGTransferVo transferVo = new AGTransferVo(agent, platformUser, AgConstants.PRE_TRANSFER,//
					agent + transferParam.getBillno(), transferParam.getType(), remit, actype, entity.getPassword(), transferParam.getCur());
			String params = this.generateAgParam(transferVo, AgConstants.AG_MD5_KEY);
			message.append(" 预转账地址：").append(AgConstants.AG_URL + AgConstants.BUSINESS_FUNCTION).append("\n");
			message.append(" 预转账参数：").append(params).append("\n");
			String result = StringsUtil.sendPost1(AgConstants.AG_URL + AgConstants.BUSINESS_FUNCTION, params);
			message.append(" 预转账接口返回：").append(result).append("\n");
			logger.info("ag transfer method tc billno = {},result = {}",transferParam.getBillno(),result);
			
			XmlUtil readXml = new XmlUtil(result);
			result = readXml.getAttribute("/result/@info").get(0).getValue();
			msg = readXml.getAttribute("/result/@msg").get(0).getValue();
			
			if (AgConstants.TRANS_SUCCESS.equals(result)) {
				logger.info("ag预转账成功");
				transferVo = new AGTransferVo(agent, platformUser, AgConstants.CONFIRM_TRANSFER, agent + transferParam.getBillno(), //
						transferParam.getType(), remit, actype, AGTransferVo.FLAG_SUCCESS, entity.getPassword(), transferParam.getCur());
				params = generateAgParam(transferVo, AgConstants.AG_MD5_KEY);
				message.append(" 转账地址：").append(AgConstants.AG_URL + AgConstants.BUSINESS_FUNCTION).append("\n");
				message.append(" 转账参数：").append(params).append("\n");
				result = StringsUtil.sendPost1(AgConstants.AG_URL + AgConstants.BUSINESS_FUNCTION, params);
				message.append(" 转账接口返回：").append(result).append("\n");
				logger.info("ag transfer method tcc billno = {},result = {}",transferParam.getBillno(),result);
				
				readXml = new XmlUtil(result);
				result = readXml.getAttribute("/result/@info").get(0).getValue();
				msg = readXml.getAttribute("/result/@msg").get(0).getValue();
				if (AgConstants.TRANS_SUCCESS.equals(result)) {
					logger.info("ag确认转账成功,type= {}", transferParam.getType());
					agRecord = this.transferRecordService.update(SysConstants.Record.TRANS_SUCCESS, "AG转账成功", agRecord);
					return JSONUtils.map2Json(success("AG转账成功"));
				}
			}
			logger.info("ag转账异常");
			agRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "AG转账异常", agRecord);
			message.insert("AG Transfer".length(),"转账异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ag transfer",message.toString().replace("&", "*"));

		} catch (Exception e) {
			logger.error("ag转账异常:", e);
			agRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "AG转账异常", agRecord);
			message.insert("AG Transfer".length(),"转账异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ag transfer",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("AG转账异常"));
		}
		return JSONUtils.map2Json(maybe("AG转账异常:" + msg));
	}

	@Override
	public String queryBalance(QueryBalanceParam param) {
		StringBuilder message = new StringBuilder();
		Map<String, Object> resultMap = null;
		String result = null;
		
		try {
			ApiInfoEntity entity = param.getEntity();
			String platformUser = entity.getPrefix().concat(param.getUsername());
			int actype = SysConstants.TRY_PLAY.equals(entity.getIsDemo() + "") ? 0 : 1;
			message.append("AG qeuryBalance 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			//1.查询余额
			AGTransferVo vo = new AGTransferVo(entity.getAgent(), platformUser, AgConstants.QUERY_BALANCE, actype, entity.getPassword(), entity.getCurrencyType());
			String params = this.generateAgParam(vo, AgConstants.AG_MD5_KEY);
			message.append("请求地址：").append(AgConstants.AG_URL + AgConstants.BUSINESS_FUNCTION).append("\n");
			message.append("请求参数：").append(params).append("\n");
			result = StringsUtil.sendPost1(AgConstants.AG_URL + AgConstants.BUSINESS_FUNCTION, params);
			message.append("接口返回：").append(result);
			logger.info("username = {},result = {}",platformUser,result);
			//2.解析
			XmlUtil xml = new XmlUtil(result);
			result = xml.getAttribute("/result/@info").get(0).getValue();
			resultMap = (isNumeric(result)) ? success(result) : failure(xml.getAttribute("/result/@msg").get(0).getValue());
			resultMap.put("balance", result);
			//正则判断值是否标准金额
			if(BigDemicalUtil.checkoutMoney(result)){
				//查询到的余额添加到缓存，用于批量查询,缓存7天自动清除
				this.redisTemplate.opsForValue().set(entity.getLiveId()+"_"+entity.getSiteId()+"_"+platformUser, result);
				this.redisTemplate.expire(entity.getLiveId()+"_"+entity.getSiteId()+"_"+platformUser,60*24*7, TimeUnit.MINUTES);
			}
		} catch (Exception e) {
			logger.info("查询余额异常");
			resultMap = maybe("query balance exception");
			message.insert("AG qeuryBalance".length(),"查询余额异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ag queryBalance",message.toString().replace("&","*"));
		}
		return JSONUtils.map2Json(resultMap);
	}

	@Override
	public String login(LoginParam param) {
		StringBuilder message = new StringBuilder();
		String result = AgConstants.LOGIN_URL + AgConstants.LOGIN_FUNCTION + "?";
		String returnUrl = "";
		try {
			ApiInfoEntity entity = param.getEntity();
			String loginAssignType = param.getLoginAssignType();
			String platformUser = entity.getPrefix().concat(param.getUsername());
			AgApiUserEntity user = this.queryUserExist(platformUser);
			int actype = SysConstants.TRY_PLAY.equals(entity.getIsDemo() + "") ? 0 : 1;
			
			message.append("AG login 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			if(StringsUtil.isNull(param.getLogoutRedirectUrl())){
				returnUrl = entity.getWebUrl();
			}else{
				returnUrl = param.getLogoutRedirectUrl();
			}
			//登录
			if("app".equals(loginAssignType)){
				StringBuffer pm = new StringBuffer();
				PHPDESEncrypt desEn = new PHPDESEncrypt(AgConstants.AG_ENCRYPT_KEY);//ag发送加密
				String p = URLEncoder.encode(desEn.encrypt(entity.getPassword()),"UTF-8");
				String u = URLEncoder.encode(platformUser,"UTF-8");
				result = pm.append("aggaming://login?").append("u=").append(u).append("&p=").append(p).append("&pid=").append(AgConstants.AG_PID).toString();
			}else{
				AGTransferVo vo = new AGTransferVo(entity.getAgent(), platformUser, entity.getPassword(), actype, "1", returnUrl, entity.getAgent() + System.currentTimeMillis(), user.getCurrencyType(), user.getOddtype(), param.getGameType());
				String loginParam = this.generateAgParam(vo, AgConstants.AG_MD5_KEY);
				result += loginParam;
			}
			logger.info("return result = {}", result);
			return JSONUtils.map2Json(success(result));
		} catch (Exception e) {
			message.insert("AG login".length(),"登录异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ag login",message.toString().replace("&","*"));
			logger.error("登录异常 : ", e);
		}
		return JSONUtils.map2Json(failure("失败或异常"));
	}
	
	@Override
	public String loginBySingGame(LoginParam param) {
		return this.login(param);
	}

	@Override
	public AgApiUserEntity queryUserExist(String username) {
		AgApiUserEntityExample agApiUserExample = new AgApiUserEntityExample();
		agApiUserExample.createCriteria().andUsernameEqualTo(username);
		List<AgApiUserEntity> list = agApiUserEntityMapper.selectByExample(agApiUserExample);
		return (list != null && list.size() > 0) ? list.get(0) : null;
	}

	@Override
	@Transactional
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			ApiInfoEntity entity = param.getEntity();
			String cur = StringsUtil.isNull(param.getCur()) ? SysConstants.CUR : param.getCur();
			String oddType = StringsUtil.isNull(param.getOddtype()) ? entity.getAgOddType(): param.getOddtype();
			String platformUser = entity.getPrefix().concat(param.getUsername());
			int actype = SysConstants.TRY_PLAY.equals(entity.getIsDemo() + "") ? 0 : 1;
			//1.用户存在?
			AgApiUserEntity user = this.queryUserExist(platformUser);
			if (user == null) {
				//1.1 ag创建会员
				logger.info("开始创建ag会员 username = {}", platformUser);
				resultMap = this.createMemberByAg(entity, platformUser, entity.getPassword(), oddType, cur, actype, resultMap);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					logger.info("开始创建本地会员 username = {}", platformUser);
					//1.2 本地创建会员
					user = this.createMemberByLocal(entity, platformUser, entity.getPassword(), cur, oddType);
				}
				return resultMap;
			}
		} catch (Exception e) {
			logger.error("创建ag会员失败 : ", e);
			return maybe("创建ag失败");
		}
		return success("user exists!");
	}

	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		StringBuilder message = new StringBuilder();
		String result = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			String billno = param.getBillno();
			message.append("AG queryStatusByBillno 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			logger.info("agent = {}, billno = {}, isDemo = {}, cur = {}, entity is null ? {}", entity.getAgent(), billno, entity.getIsDemo(), entity.getCurrencyType(), entity == null);
			AGTransferVo vo = new AGTransferVo(entity.getAgent(), AgConstants.QUERY_ORDER_STATUS, entity.getAgent() + billno, entity.getIsDemo(), entity.getCurrencyType());
			result = this.generateAgParam(vo, AgConstants.AG_MD5_KEY);
			message.append("请求地址：").append(AgConstants.AG_URL + AgConstants.BUSINESS_FUNCTION).append("\n");
			message.append("请求参数：").append(result).append("\n");
			result = StringsUtil.sendPost1(AgConstants.AG_URL + AgConstants.BUSINESS_FUNCTION, result);
			message.append("接口返回：").append(result).append("\n");
			logger.info("billno = {},result = {}",billno,result);
			XmlUtil readXml = new XmlUtil(result);
			result = readXml.getAttribute("/result/@info").get(0).getValue();
			if ("0".equals(result)) {
				result = readXml.getAttribute("/result/@msg").get(0).getValue();
				return JSONUtils.map2Json(success(result));
			} else if ("2".equals(result)) {
				result = "因无效的转账金额引致的失败";
			} else if ("network_error".equals(result)) { //因网络错误,不能确定转账状态是否成功
				message.insert("AG queryStatusByBillno".length(),"查询订单状态异常！").append("\n");
				telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ag queryStatusByBillno",message.toString().replace("&","*"));
				return JSONUtils.map2Json(maybe(result));
			}
		} catch (Exception e) {
			logger.info("查询订单状态出错 : ", e);
			message.insert("AG queryStatusByBillno".length(),"查询订单状态异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ag queryStatusByBillno",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("查询订单状态出错 : " + result));
		}
		
		message.insert("AG queryStatusByBillno".length(),"查询订单状态异常！").append("\n");
		telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ag queryStatusByBillno",message.toString().replace("&","*"));
		return JSONUtils.map2Json(failure(result));
	}

	/**
	 * ag创建会员
	 */
	private Map<String, Object> createMemberByAg(ApiInfoEntity entity, String username, String password, String oddtype, String cur, int actype, Map<String, Object> resultMap) {
		StringBuilder message = new StringBuilder();
		String result = null;
		try {
			message.append("AG createMemberByAg 站点：").append(entity.getSiteId());
			message.append(",会员：").append(username);
			
			AGTransferVo vo = new AGTransferVo(entity.getAgent(), username, AgConstants.CREATE_MEMBER, actype, password, cur, oddtype);
			result = this.generateAgParam(vo, AgConstants.AG_MD5_KEY);
			message.append("请求地址：").append(AgConstants.CREATE_MEMBER_URL + AgConstants.BUSINESS_FUNCTION).append("\n");
			message.append("请求参数：").append(result).append("\n");
			
			result = StringsUtil.sendPost1(AgConstants.CREATE_MEMBER_URL + AgConstants.BUSINESS_FUNCTION, result);
			message.append("接口返回：").append(result).append("\n");
			logger.info("ag创建会员result = {}", result);
			
			XmlUtil xml = new XmlUtil(result);
			result = xml.getAttribute("/result/@info").get(0).getValue();
			return AgConstants.TRANS_SUCCESS.equals(result) ? success(resultMap, "success!") : failure(xml.getAttribute("/result/@msg").get(0).getValue());
		} catch (Exception e) {
			logger.error("创建ag会员失败 : ", e);
			message.insert("AG createMemberByAg".length(),"创建会员异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-ag createMemberByAg",message.toString().replace("&","*"));
			return maybe("创建ag会员失败 ");
		}
	}

	/**
	 * 创建会员
	 */
	private AgApiUserEntity createMemberByLocal(ApiInfoEntity entity, String username, String password, String cur, String oddtype) {
		AgApiUserEntity userEntity = new AgApiUserEntity();
		userEntity.setUsername(username);
		userEntity.setPassword(password);
		userEntity.setApiInfoId(entity.getId().intValue());// 对应代理 id
		userEntity.setAgentName(entity.getAgent()); //ag 代理名
		userEntity.setCreateTime(DateUtil.getCurrentTime());//时间
		userEntity.setOddtype(oddtype);
		userEntity.setCurrencyType(cur);//货币类型
		userEntity.setUserStatus(1);
		userEntity.setSiteId(entity.getSiteId());
		userEntity.setSiteName(entity.getProjectAgent());
		int insert = agApiUserEntityMapper.insert(userEntity); //插入 AG代理
		logger.info("本地创建会员username = {}, result = {}", username, insert);
		return userEntity;
	}

	/**
	 * 生成ag参数
	 */
	private String generateAgParam(AGTransferVo transferVo, String keyDe) throws Exception {
		String params = ReflectUtil.generateParam(transferVo, AgConstants.AG_PARAM_JOIN);
		logger.info("agent = {}, ag 明文参数 = {}", transferVo.getCagent(), params);
		PHPDESEncrypt desEn = new PHPDESEncrypt(AgConstants.AG_ENCRYPT_KEY);//ag发送加密
		params = desEn.encrypt(params);
		String key = StringsUtil.toMD5(params + keyDe);
		return "params=" + params + "&" + "key=" + key;
	}

	public boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[-\\+]?[0-9]*.?[0-9]*");
		return pattern.matcher(str).matches();
	}

	@Override
	public String queryAgentBalance(QueryBalanceParam param) {
		Map<String, Object> resultMap = null;
		Double balance = 0.00;
		ApiInfoEntity entity = null;
		String agent = null;
		String siteId = null;
		try {
			entity = param.getEntity();
			agent = param.getAgent();
			siteId = String.valueOf(entity.getSiteId());
			logger.info("ag 查询redis缓存代理总额度 agent{}, siteId{}",agent,siteId);
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
			logger.error("siteId={},agent={},查询代理总余额异常 : error={}",siteId,agent, e);
			resultMap = maybe("查询余额异常");
		}
		return JSONUtils.map2Json(resultMap);
	}
}
