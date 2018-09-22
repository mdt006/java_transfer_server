package com.ds.transfer.og.service;

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

import com.ds.msg.TelegramMessage;
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
import com.ds.transfer.og.constants.OgConstants;
import com.ds.transfer.og.constants.TelegramConstants;
import com.ds.transfer.og.properties.LoadProps;
import com.ds.transfer.og.util.DigitalUtil;
import com.ds.transfer.og.util.EncUtil;
import com.ds.transfer.og.vo.OgTransferVo;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.OgApiUserEntity;
import com.ds.transfer.record.entity.OgApiUserEntityExample;
import com.ds.transfer.record.entity.OgApiUserEntityExample.Criteria;
import com.ds.transfer.record.entity.TransferRecordEntity;
import com.ds.transfer.record.mapper.OgApiUserEntityMapper;
import com.ds.transfer.record.service.TransferRecordService;

@Service
public class TransferServiceImpl extends CommonTransferService implements TransferService<OgApiUserEntity> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource(name = "moneyCenterService")
	private TransferService<?> moneyCenterService;

	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;

	@Resource
	private OgApiUserEntityMapper ogApiUserEntityMapper;
	
	@Autowired
	protected RedisTemplate<String, String> redisTemplate;
	
	//小飞机消息组件
	TelegramMessage telegramMessage = TelegramMessage.getInstance();

	@Override
	public String transfer(TransferParam transferParam) {
		StringBuilder message = new StringBuilder();
		TransferRecordEntity ogRecord = new TransferRecordEntity();
		String typeDes = null;
		String result = null;
		try {
			ApiInfoEntity entity = transferParam.getEntity();
			String platformUser = entity.getPrefix() + transferParam.getUsername();
			String type = transferParam.getType();
			typeDes = IN.equals(type) ? "入" : "出";
			
			message.append("OG Transfer 站点：").append(entity.getSiteId());
			message.append(",会员：").append(transferParam.getUsername());
			message.append(",转账：").append(transferParam.getType().equals("IN")==true?"DS-->OG":"OG-->DS");
			message.append(",单号:").append(transferParam.getBillno());
			message.append(",金额：").append(transferParam.getCredit()).append("\n");;
			
			//1.插入记录
			ogRecord = this.transferRecordService.insert(entity.getSiteId(), entity.getLiveId(), transferParam.getTransRecordId(), entity.getPassword(), transferParam.getUsername(), transferParam.getCredit(), transferParam.getBillno(),//
					type, entity.getLiveName(), transferParam.getRemark(), ogRecord);
			logger.info("OG视讯 转账记录插入成功,username={},id = {}",transferParam.getUsername(),ogRecord.getId());
			//2.转账
			OgTransferVo vo = new OgTransferVo(entity.getAgent(), platformUser, entity.getPassword(), entity.getAgent() + transferParam.getBillno(),//
					type, transferParam.getCredit(), OgConstants.TransMethods.PREPARED_TRANS);
			String voParam = ReflectUtil.generateParam(vo, "$");
			message.append("预转账请求地址：").append(LoadProps.getProperty("og_transfer_url")).append("\n");
			message.append("预转账明文参数：").append(voParam).append("\n");
			logger.info("调用 OG PTC before siteId={},username={}, 明文参数 ：{}",entity.getSiteId(),transferParam.getUsername(),voParam);
			String params = EncUtil.encodeStrBase64(voParam);
			message.append("预转账密文参数：").append(params).append("\n");
			String key = EncUtil.toMD5(params + LoadProps.getProperty("og_key"));
			logger.info("调用 OG PTC before username={},siteId={},预转账接口密文参数：{}, key = {}",transferParam.getUsername(),entity.getSiteId(),params,key);
			message.append("预转账请求报文:").append(LoadProps.getProperty("og_transfer_url").replaceAll(":params", params).replaceAll(":key", key)).append("\n");
			long ptcStartTime = System.currentTimeMillis();
			result = StringsUtil.sendGet(LoadProps.getProperty("og_transfer_url").replaceAll(":params", params).replaceAll(":key", key));
			long ptcEndTime = System.currentTimeMillis();
			message.append("预转账接口返回:").append(result).append("\n");
			logger.info("调用 OG PTC after username={},siteId={},billno={},elapsedTime={} ms OG预转账接口返回：{}",transferParam.getUsername(),
					entity.getSiteId(),transferParam.getBillno(),(ptcEndTime-ptcStartTime),result);
			result = new XmlUtil(result).getSelectNodes("/result").get(0).getStringValue();
			if ("1".equals(result)) {
				vo = new OgTransferVo(entity.getAgent(), platformUser, entity.getPassword(), entity.getAgent() + transferParam.getBillno(),//
						type, transferParam.getCredit(), OgConstants.TransMethods.CONFIRM_TRANS);
				voParam = ReflectUtil.generateParam(vo, "$");
				
				message.append("转账请求地址：").append(LoadProps.getProperty("og_transfer_url")).append("\n");
				message.append("转账明文参数：").append(voParam).append("\n");
				logger.info("调用 OG CTC before siteId={},username={}, 明文参数 ：{}",entity.getSiteId(),transferParam.getUsername(),voParam);
				params = EncUtil.encodeStrBase64(voParam);
				message.append("转账密文参数：").append(params).append("\n");
				key = EncUtil.toMD5(params + LoadProps.getProperty("og_key"));
				logger.info("调用 OG CTC before siteId={},username={},转账接口密文参数：{}, key = {}",transferParam.getUsername(),entity.getSiteId(),params,key);
				
				message.append("转账请求报文:").append(LoadProps.getProperty("og_transfer_url").replaceAll(":params", params).replaceAll(":key", key)).append("\n");
				long ctcStartTime = System.currentTimeMillis();
				result = StringsUtil.sendGet(LoadProps.getProperty("og_transfer_url").replaceAll(":params", params).replaceAll(":key", key));
				long ctcEndTime = System.currentTimeMillis();
				message.append("转账接口返回:").append(result).append("\n");
				logger.info("调用 OG CTC after siteId={},username={},billno={},elapsedTime={} ms OG转账接口返回：{}",transferParam.getUsername(),
						entity.getSiteId(),transferParam.getBillno(),(ctcEndTime-ctcStartTime),result);
				result = new XmlUtil(result).getSelectNodes("/result").get(0).getStringValue();
				if ("1".equals(result)) {
					ogRecord = this.transferRecordService.update(SysConstants.Record.TRANS_SUCCESS, "OG转" + typeDes + "成功", ogRecord);
					return JSONUtils.map2Json(success("OG 转" + typeDes + "成功!"));
				}
			}
			ogRecord = this.transferRecordService.update(SysConstants.Record.TRANS_FAILURE, "OG转" + typeDes + "失败", ogRecord);
			message.insert("OG Transfer".length(),"转账异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-og transfer",message.toString().replace("&", "*"));
			
			if (IN.equals(type)) {
				logger.info("OG转入失败,退回DS主账户");
				transferParam.setRemark("OG转入失败,退回DS主账户");
				transferParam.setBillno(transferParam.getBillno() + "F");
				transferParam.setUsername(transferParam.getUsername());
				logger.info("调用DS钱包 before OG转入失败退回钱包 username={},billno={}",transferParam.getUsername(),transferParam.getBillno() + "F");
				long moneyStartTime = System.currentTimeMillis();
				result = this.moneyCenterService.transfer(transferParam);
				long moneyEndTime = System.currentTimeMillis();
				logger.info("调用DS钱包 after OG转入失败退回钱包 username={},billno={},elapsedTime={} ms,network result={}",
						transferParam.getBillno() + "F",transferParam.getUsername(),(moneyEndTime-moneyStartTime),result);
				Map<String, Object> resultMap = JSONUtils.json2Map(result);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					return JSONUtils.map2Json(failure(resultMap, "OG转入失败:" + result + ",退回DS主账户"));
				}
			}
			return JSONUtils.map2Json(failure("OG 转" + typeDes + "失败 : " + result));
		} catch (Exception e) {
			logger.error("OG 转" + typeDes + "异常 : ", e);
			ogRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "OG转" + typeDes + "异常", ogRecord);
			message.insert("OG Transfer".length(),"转账异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-og transfer",message.toString().replace("&", "*"));
			return JSONUtils.map2Json(maybe("OG 转" + typeDes + "异常"));
		}
	}

	@Override
	public String queryBalance(QueryBalanceParam param) {
		StringBuilder message = new StringBuilder();
		Map<String, Object> resultMap = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			String platformUser = entity.getPrefix().concat(param.getUsername());
			message.append("OG qeuryBalance 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			//1.查询余额
			OgTransferVo vo = new OgTransferVo(entity.getAgent(), platformUser, entity.getPassword(), OgConstants.TransMethods.QUERY_BALANCE);
			String generateParam = ReflectUtil.generateParam(vo, "$");
			message.append("请求明文参数：").append(generateParam).append("\n");
			String params = EncUtil.encodeStrBase64(generateParam);
			String key = EncUtil.toMD5(params + LoadProps.getProperty("og_key"));
			message.append("请求密文参数：").append(params).append("\n");
			logger.info("调用 OG GB 查余额  before siteId={},username={},明文参数 param={}",entity.getSiteId(),param.getUsername(),generateParam);
			logger.info("调用 OG GB 查余额  before siteId={},username={},密文参数 aram={},key={}",entity.getSiteId(),param.getUsername(),params,key);
			message.append("请求报文：").append(LoadProps.getProperty("og_url").replaceAll(":params", params).replaceAll(":key", key));
			long gbStartTime = System.currentTimeMillis();
			String result = StringsUtil.sendGet(LoadProps.getProperty("og_url").replaceAll(":params", params).replaceAll(":key", key));
			long gbEndTime = System.currentTimeMillis();
			message.append("接口返回：").append(result).append("\n");
			
			logger.info("调用 OG GB 查余额  after siteId={},username={},elapsedTime={} ms, OG查余额接口返回：{}",
					entity.getSiteId(),param.getUsername(),(gbEndTime-gbStartTime),result);
			//2.解析
			result = new XmlUtil(result).getSelectNodes("/result").get(0).getStringValue();
			if ("10".equals(result)) {
				return JSONUtils.map2Json(failure("The agent not exist"));
			}
			if (DigitalUtil.isMoney(result)) {
				resultMap = success("success!");
				resultMap.put("balance", result);
				//正则判断值是否标准金额
				if(BigDemicalUtil.checkoutMoney(result)){
					//查询到的余额添加到缓存，用于批量查询,缓存7天自动清除
					this.redisTemplate.opsForValue().set(entity.getLiveId()+"_"+entity.getSiteId()+"_"+platformUser, result);
					this.redisTemplate.expire(entity.getLiveId()+"_"+entity.getSiteId()+"_"+platformUser,60*60*24*7, TimeUnit.SECONDS);
				}
			} else {
				resultMap = failure(result);
			}
		} catch (Exception e) {
			logger.info("查询余额异常",e);
			resultMap = maybe("查询余额异常");
			message.insert("OG qeuryBalance".length(),"查询余额异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-og queryBalance",message.toString().replace("&","*"));
		}
		return JSONUtils.map2Json(resultMap);
	}

	@Override
	public String login(LoginParam param) {
		StringBuilder message = new StringBuilder();
		String result = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			String platformUser = entity.getPrefix().concat(param.getUsername());
			
			message.append("OG login 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			OgTransferVo vo = new OgTransferVo(entity.getAgent(), platformUser, entity.getPassword(), entity.getWebUrl(), param.getGameType(), param.getGamekind(),//
					LoadProps.getProperty("og_plat"), param.getLanguage(), OgConstants.TransMethods.LOGIN);
			String params = EncUtil.encodeStrBase64(ReflectUtil.generateParam(vo, "$"));
			message.append("请求参数:").append(params).append("\n");
			String key = EncUtil.toMD5(params + LoadProps.getProperty("og_key"));
			result = LoadProps.getProperty("og_url").replaceAll(":params", params).replaceAll(":key", key);
			message.append("返回报文：").append(result).append("\n");
			logger.info("result = {}", result);
		} catch (Exception e) {
			logger.error("登录异常 : ", e);
			message.insert("OG login".length(),"登陆异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-og login",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("登录异常"));
		}
		return JSONUtils.map2Json(success(result));
	}

	@Override
	public String loginBySingGame(LoginParam param) {
		this.login(param);
		return null;
	}

	@Override
	public OgApiUserEntity queryUserExist(String username) {
		OgApiUserEntityExample example = new OgApiUserEntityExample();
		Criteria createCriteria = example.createCriteria();
		createCriteria.andUsernameEqualTo(username);
		List<OgApiUserEntity> list = this.ogApiUserEntityMapper.selectByExample(example);
		return (list == null || list.size() <= 0) ? null : list.get(0);
	}

	@Override
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		StringBuilder message = new StringBuilder();
		try {
			ApiInfoEntity entity = param.getEntity();
			String platformUser = entity.getPrefix().concat(param.getUsername());
			
			message.append("OG checkAndCreateMember 站点：").append(entity.getSiteId());
			message.append(",会员：").append(platformUser);
			OgApiUserEntity user = this.queryUserExist(platformUser);
			
			
			if (user == null) {
				OgTransferVo vo = new OgTransferVo(entity.getAgent(), platformUser, param.getCur(), entity.getPassword(),//
						param.getOddtype(), "38", "20", OgConstants.TransMethods.CHECK_AND_CREATE_ACCOUNT);
				String params = ReflectUtil.generateParam(vo, "$");
				message.append("请求明文参数：").append(params).append("\n");
				logger.info("调用 OG CACA 创建会员  before siteId={},username={},明文参数 param={},key={}",entity.getSiteId(),param.getUsername(),params);
				String base64Params = EncUtil.encodeStrBase64(params);
				message.append("请求密文参数：").append(base64Params).append("\n");
				
				String key = EncUtil.toMD5(base64Params + LoadProps.getProperty("og_key"));
				logger.info("调用 OG CACA 创建会员  before siteId={},username={},密文参数 param={},key={}",entity.getSiteId(),param.getUsername(),base64Params,key);
				
				message.append("请求报文：").append(LoadProps.getProperty("og_url").replaceAll(":params", base64Params).replaceAll(":key", key));
				long cacaStartTime = System.currentTimeMillis();
				String result = StringsUtil.sendGet(LoadProps.getProperty("og_url").replaceAll(":params", base64Params).replaceAll(":key", key));
				long cacaEndTime = System.currentTimeMillis();
				message.append("接口返回：").append(result).append("\n");
				
				logger.info("调用 OG CACA 创建会员  after siteId={},username={},elapsedTime={} ms, OG创建会员接口返回：{}",
						entity.getSiteId(),param.getUsername(),(cacaEndTime-cacaStartTime),result);
				
				result = new XmlUtil(result).getSelectNodes("/result").get(0).getStringValue();
				if ("1".equals(result)) {//success
					logger.info("创建og会员成功!");
					user = this.createMemberByLocal(param, entity, platformUser);
					logger.info("创建本地会员 = {} 成功!",param.getUsername());
					return success("成功!");
				} else if ("0".equals(result)) {
					return success("Maybe username = "+param.getUsername()+", already exist!");
				} else if ("2".equals(result)) {
					message.insert("OG checkAndCreateMember".length(),"创建会员异常！").append("\n");
					telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-og checkAndCreateMember",message.toString().replace("&","*"));
					return failure("the password no right!");
				} else if ("3".equals(result)) {
					message.insert("OG checkAndCreateMember".length(),"创建会员异常！").append("\n");
					telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-og checkAndCreateMember",message.toString().replace("&","*"));
					return failure("Username is too long!");
				} else if ("10".equals(result)) {
					message.insert("OG checkAndCreateMember".length(),"创建会员异常！").append("\n");
					telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-og checkAndCreateMember",message.toString().replace("&","*"));
					return failure("The agent not exist");
				} else {
					message.insert("OG checkAndCreateMember".length(),"创建会员异常！").append("\n");
					telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-og checkAndCreateMember",message.toString().replace("&","*"));
					return failure("OG return : " + result);
				}
			}
			return success("用户已经存在!");
		} catch (Exception e) {
			logger.error("系统内部出错 : ", e);
			message.insert("OG checkAndCreateMember".length(),"创建会员异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-og checkAndCreateMember",message.toString().replace("&","*"));
			return maybe("系统内部错误");
		}
	}

	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		StringBuilder message = new StringBuilder();
		ApiInfoEntity entity = param.getEntity();
		String result = null;
		try {
			
			message.append("OG queryStatusByBillno 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername()).append("\n");
			
			OgTransferVo vo = new OgTransferVo(entity.getAgent(), param.getUsername(), param.getPassword(),
					entity.getAgent() + param.getBillno(),
					param.getType(), param.getCredit(), OgConstants.TransMethods.CONFIRM_TRANS);
			String voParam = ReflectUtil.generateParam(vo, "$");
			message.append("请求明文参数：").append(voParam).append("\n");
			logger.info("调用 OG CTC 检查转账状态   before siteId={},username={},明文参数 param={},key={}",entity.getSiteId(),param.getUsername(),voParam);
			String params = EncUtil.encodeStrBase64(voParam);
			String key = EncUtil.toMD5(params + LoadProps.getProperty("og_key"));
			message.append("请求密文参数：").append(params).append("\n");
			logger.info("调用 OG CTC 检查转账状态   before siteId={},username={},密文参数 param={},key={}",entity.getSiteId(),param.getUsername(),params,key);
			message.append("请求报文：").append(LoadProps.getProperty("og_url").replaceAll(":params", params).replaceAll(":key", key)).append("\n");
			long ctcStartTime = System.currentTimeMillis();
			result = StringsUtil.sendGet(LoadProps.getProperty("og_url").replaceAll(":params", params).replaceAll(":key", key));
			long ctcEndTime = System.currentTimeMillis();
			message.append("接口返回：").append(result).append("\n");
			logger.info("调用 OG CTC 检查转账状态  after siteId={},username={},elapsedTime={} ms, OG检查转账状态接口返回：{}",
					entity.getSiteId(),param.getUsername(),(ctcEndTime-ctcStartTime),result);
			result = new XmlUtil(result).getSelectNodes("/result").get(0).getStringValue();
			if ("1".equals(result)) {
				return JSONUtils.map2Json(success("success!"));
			} else if ("Network_error".equals(result)) {
				message.insert("OG queryStatusByBillno".length(),"查询订单异常！").append("\n");
				telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-og queryStatusByBillno",message.toString().replace("&","*"));
				return JSONUtils.map2Json(maybe("网络延迟,请重新再试!"));
			}
		} catch (Exception e) {
			logger.error("OG查询订单出错 : ", e);
			message.insert("OG queryStatusByBillno".length(),"查询订单异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-og queryStatusByBillno",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("系统内部出错"));
		}
		
		message.insert("OG queryStatusByBillno".length(),"查询订单异常！").append("\n");
		telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-og queryStatusByBillno",message.toString().replace("&","*"));
		return JSONUtils.map2Json(failure("result = " + result));
	}

	private OgApiUserEntity createMemberByLocal(UserParam param, ApiInfoEntity entity, String username) {
		OgApiUserEntity user = new OgApiUserEntity();
		user = new OgApiUserEntity();
		user.setAgentName(entity.getAgent());
		user.setApiInfoId(entity.getId().intValue());
		user.setCreateTime(DateUtil.getCurrentTime());
		user.setCurrencyType("RMB");
		user.setOddtype(SysConstants.ODD_TYPE);
		user.setPassword(entity.getPassword());
		user.setSiteId(entity.getSiteId());
		user.setSiteName(entity.getProjectAgent());
		user.setUsername(username);
		user.setUserStatus(1);
		this.ogApiUserEntityMapper.insert(user);
		return user;
	}

	@Override
	public String queryAgentBalance(QueryBalanceParam param) {
		Map<String, Object> resultMap = null;
		double balance = 0.00;  
		try {
			ApiInfoEntity entity = param.getEntity();
			String siteId = String.valueOf(entity.getSiteId());
			logger.info("og 查询redis缓存代理总额度 agent{}, siteId{}",param.getAgent(),siteId);
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
}
