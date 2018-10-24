package com.ds.transfer.h8.service;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
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
import com.ds.transfer.common.util.XmlUtil;
import com.ds.transfer.common.vo.LoginParam;
import com.ds.transfer.common.vo.QueryBalanceParam;
import com.ds.transfer.common.vo.QueryOrderStatusParam;
import com.ds.transfer.common.vo.TransferParam;
import com.ds.transfer.common.vo.UserParam;
import com.ds.transfer.h8.constants.H8Constants;
import com.ds.transfer.h8.constants.TelegramConstants;
import com.ds.transfer.h8.vo.H8TransferVo;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.BetLimit;
import com.ds.transfer.record.entity.BetLimitExample;
import com.ds.transfer.record.entity.H8ApiUserEntity;
import com.ds.transfer.record.entity.H8ApiUserEntityExample;
import com.ds.transfer.record.entity.H8ApiUserEntityExample.Criteria;
import com.ds.transfer.record.entity.TransferRecordEntity;
import com.ds.transfer.record.mapper.BetLimitMapper;
import com.ds.transfer.record.mapper.H8ApiUserEntityMapper;
import com.ds.transfer.record.service.TransferRecordService;

public class TransferServiceImpl extends CommonTransferService implements TransferService<H8ApiUserEntity> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;
	@Resource(name = "moneyCenterService")
	private TransferService<?> moneyCenterService;
	@Resource
	private H8ApiUserEntityMapper h8ApiUserEntityMapper;
	@Autowired
	protected RedisTemplate<String, String> redisTemplate;
	@Autowired
	private BetLimitMapper betLimitMapper;
	//小飞机消息组件
	TelegramMessage telegramMessage = TelegramMessage.getInstance();
	
	@Override
	public String transfer(TransferParam transferParam) {
		StringBuilder message = new StringBuilder();
		TransferRecordEntity record = new TransferRecordEntity();
		String msg = "";
		try {
			ApiInfoEntity entity = transferParam.getEntity();
			String type = transferParam.getType();
			
			message.append("H8 Transfer 站点：").append(entity.getSiteId());
			message.append(",会员：").append(transferParam.getUsername());
			message.append(",转账：").append(transferParam.getType().equals("IN")==true ? "DS-->H8":"H8-->DS");
			message.append(",单号:").append(transferParam.getBillno());
			message.append(",金额：").append(transferParam.getCredit()).append("\n");;
			
			record = this.transferRecordService.insert(entity.getSiteId(), entity.getLiveId(), transferParam.getTransRecordId(), entity.getPassword(), transferParam.getUsername(), transferParam.getCredit(), //
					transferParam.getBillno(), type, H8Constants.H8, transferParam.getRemark(), record);
			logger.info("h8转账记录,  id = {}", record.getId());
			
			H8TransferVo vo = new H8TransferVo(entity.getPassword(), entity.getAgent(), transferParam.getUsername(),//
					IN.equals(type) ? H8Constants.function.TRANSFER_IN : H8Constants.function.TRANSFER_OUT,//
					transferParam.getBillno(), transferParam.getCredit());
			
			String params = ReflectUtil.generateParam(vo);
			message.append(" 转账地址：").append(entity.getReportUrl()).append("\n");
			message.append(" 转账参数：").append(params).append("\n");
			String result = StringsUtil.sendGet(H8Constants.URL, params);
			message.append(" 转账接口返回：").append(result).append("\n");
			
			XmlUtil xml = new XmlUtil(result);
			String errcode = xml.getSelectNodes("/response/errcode").get(0).getStringValue();
			msg = xml.getSelectNodes("/response/errtext").get(0).getStringValue();
			
			if (H8Constants.STATE_SUCCESS.equals(errcode)) {
				logger.info("h8转账成功,type = {}", type);
				record = this.transferRecordService.update(SysConstants.Record.TRANS_SUCCESS, "H8转账成功", record);
				return JSONUtils.map2Json(success("H8 转账成功"));
			}
			logger.info("h8转账失败");
			record = this.transferRecordService.update(SysConstants.Record.TRANS_FAILURE, "H8转账失败", record);
			
			message.insert("H8 Transfer".length(),"转账异常！").append("\n");
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-h8 transfer",message.toString().replace("&", "*"));
			
			if (IN.equals(type)) {
				logger.info("H8转入失败,退回DS主账户");
				transferParam.setType("H8转入失败,退回DS主账户");
				transferParam.setBillno(transferParam.getBillno() + "F");
				result = this.moneyCenterService.transfer(transferParam);
				Map<String, Object> resultMap = JSONUtils.json2Map(result);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					return JSONUtils.map2Json(failure(resultMap, "H8转入失败:" + msg + ",退回DS主账户"));
				}
				logger.info("h8 transfer siteId={},username={},billno={}转入h8失败,退回DS调用钱包失败!",entity.getSiteId(),
						transferParam.getUsername(),transferParam.getBillno());
			}
			
		} catch (Exception e) {
			logger.error("h8转账异常:", e);
			record = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, "H8转账异常", record);
			message.insert("H8 Transfer".length(),"转账异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-h8 transfer",message.toString().replace("&", "*"));
			return JSONUtils.map2Json(maybe("H8 转账异常"));
		}
		return JSONUtils.map2Json(failure("H8 转账失败:" + msg));
	}

	@Override
	public String queryBalance(QueryBalanceParam param) {
		StringBuilder message = new StringBuilder();
		Map<String, Object> resultMap = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			String username = param.getUsername();
			message.append("H8 qeuryBalance 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername()).append("\n");
			//查询余额
			H8TransferVo vo = new H8TransferVo(entity.getPassword(), entity.getAgent(), username, H8Constants.function.QUERY_BALANCE);
			String queryParam = ReflectUtil.generateParam(vo);
			
			message.append("请求地址：").append(entity.getReportUrl()).append("\n");
			message.append("请求参数：").append(queryParam).append("\n");
			String result = StringsUtil.sendGet(entity.getReportUrl(), queryParam);
			message.append("接口返回：").append(result).append("\n");
			
			XmlUtil readXml = new XmlUtil(result);
			String errcode = readXml.getSelectNodes("/response/errcode").get(0).getStringValue();
			if ("0".equals(errcode)) {
				String balance = readXml.getSelectNodes("/response/result").get(0).getStringValue();
				resultMap = success(balance);
				resultMap.put("balance", balance);
				//正则判断值是否标准金额
				if(BigDemicalUtil.checkoutMoney(balance)){
					//查询到的余额添加到缓存，用于批量查询,缓存7天自动清除
					this.redisTemplate.opsForValue().set(entity.getLiveId()+"_"+entity.getSiteId()+"_"+ param.getUsername(), balance);
					this.redisTemplate.expire(entity.getLiveId()+"_"+entity.getSiteId()+"_"+ param.getUsername(),60*60*24*7, TimeUnit.SECONDS);
				}
				return JSONUtils.map2Json(resultMap);
			}
			resultMap = failure(readXml.getSelectNodes("/response/errtext").get(0).getStringValue());
		} catch (Exception e) {
			logger.error("h8余额查询异常 : ", e);
			resultMap = maybe("h8余额查询异常");
			message.insert("H8 qeuryBalance".length(),"查询余额异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-h8 queryBalance",message.toString().replace("&","*"));
			return JSONUtils.map2Json(resultMap);
		}
		message.insert("H8 qeuryBalance".length(),"查询余额异常！").append("\n");
		telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-h8 queryBalance",message.toString().replace("&","*"));
		return JSONUtils.map2Json(resultMap);
	}

	
	
	@Override
	public String login(LoginParam param) {
		StringBuilder message = new StringBuilder();
		String result = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			String username = param.getUsername();
			
			message.append("H8 login 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			//H8修改限红
			updateH8Oddtype(param);
			if (null != param.getLottoType() && param.getLottoType().equals("PM")) {
				String[] hostArr = entity.getH8Host().split("\\.");
				entity.setH8Host(hostArr[0]+"mobi"+"."+hostArr[1]+"."+hostArr[2]);
			}
			entity.setH8Host(H8Constants.HOST);
			H8TransferVo vo = new H8TransferVo(entity.getPassword(), entity.getAgent(), username, param.getAction(),//
					entity.getH8Host(), param.getLanguage(), param.getAccType());
			result = ReflectUtil.generateParam(vo);
			message.append(" 登录地址：").append(entity.getReportUrl()).append("\n");
			message.append(" 登录参数：").append(result).append("\n");
			result = StringsUtil.sendGet(entity.getReportUrl(), result);
			message.append(" 接口返回：").append(result).append("\n");
			StringBuilder h8Url = new StringBuilder();
			Document document = DocumentHelper.parseText(result.trim());
			Element root = document.getRootElement();
			Node host = document.selectSingleNode("//host");
			List<Element> loginParam = document.selectNodes("//param");
			List<Element> elements = root.elements();
			JSONObject object = new JSONObject();
			
			for (Iterator<Element> it = elements.iterator(); it.hasNext();) {
				Element element = it.next();
				logger.info("element value######" + element);
				if ("errcode".equals(element.getName())) { // 0代表成功
					if ("0".equals(element.getText())) { //h8 登陆成功
						object.put("status", "10000");
						//h8 登陆成功 进行检索
						Node us = document.selectSingleNode("//us");
						Node k = document.selectSingleNode("//k");
						Node lang = document.selectSingleNode("//lang");
						Node accTypeNode = document.selectSingleNode("//accType");
						Node r = document.selectSingleNode("//r");
						logger.info("us test value:::::" + us.getText());
						h8Url.append(host.getText()).append("?");
						h8Url.append("us=").append(us.getText());
						h8Url.append("&k=").append(k.getText());
						h8Url.append("&lang=").append(lang.getText());
						h8Url.append("&accType=").append(accTypeNode.getText());
						h8Url.append("&r=").append(r.getText());
						logger.info("#########h8 登陆路径" + h8Url);
						return JSONUtils.map2Json(success(h8Url.toString()));
					} else {
						object.put("status", "10050");
						object.put("message", result);
						return object.toString();
					}
				}
				logger.info("H8 url 登陆路径显示:::" + h8Url);
			}
		} catch (Exception e) {
			logger.error("login error : ", e);
			message.insert("H8 login".length(),"登录异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-h8 login",message.toString().replace("&","*"));
			return JSONUtils.map2Json(maybe("系统内部异常 : " + result));
		}
		message.insert("H8 login".length(),"登录异常！").append("\n");
		telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-h8 login",message.toString().replace("&","*"));
		return JSONUtils.map2Json(failure(result));
	}

	private String updateH8Oddtype(LoginParam param){
		StringBuilder message = new StringBuilder();
		String result = null;
		String betMax = "";
		String betLim = "";
		try {
			String username = param.getUsername();
			ApiInfoEntity entity = param.getEntity();
			
			message.append("H8 updateH8Oddtype 站点：").append(entity.getSiteId());
			message.append(",会员：").append(username);
			
			BetLimitExample betLimitExample = new BetLimitExample();
			betLimitExample.createCriteria().andSiteIdEqualTo(entity.getSiteId()).andLiveIdEqualTo(String.valueOf(entity.getLiveId()));
			List<BetLimit> betLimitList = betLimitMapper.selectByExample(betLimitExample);
			
			if(null != betLimitList && betLimitList.size() > 0){
				BetLimit betLimit = betLimitList.get(0);
				betMax = StringsUtil.isNull(betLimit.getMaxLimit())?"5000":betLimit.getMaxLimit();
				betLim = StringsUtil.isNull(betLimit.getMinLimit())?"20000":betLimit.getMinLimit();
				StringBuffer params = new StringBuffer();
				params.append("action=update");
				params.append("&agent=").append(entity.getAgent());
				params.append("&username=").append(username);
				params.append("&secret=").append(entity.getSecret());
				params.append("&max1=").append(betMax);
				params.append("&max2=").append(betMax);
				params.append("&max3=").append(betMax);
				params.append("&max4=").append(betMax);
				params.append("&lim1=").append(betLim);
				params.append("&lim2=").append(betLim);
				params.append("&lim3=").append(betLim);
				params.append("&lim4=").append(betLim);
				params.append("&comtype=A");
				params.append("&com1=0");
				params.append("&com2=0");
				params.append("&com3=0");
				params.append("&com4=0");
				params.append("&com5=0");
				params.append("&com6=0");
				params.append("&com7=0");
				params.append("&com8=0");
				params.append("&com9=0");
				params.append("&suspend=0");
				logger.info("h8 udpateOddtype before params={}",params.toString());
				message.append("请求地址：").append(entity.getReportUrl()).append("\n");
				message.append("请求参数：").append(params).append("\n");
				result = StringsUtil.sendGet(entity.getReportUrl(), params.toString());
				message.append("接口返回：").append(result).append("\n");
				logger.info("h8 udpateOddtype after update username={},siteId={}, result = {}", username,entity.getSiteId(),result);
			}else{
				logger.info("h8 udpateOddtype username={},siteId={} 无配置信息,走默认限红！",username,entity.getSiteId());
			}
		}catch(Exception e){
			logger.error("h8 udpateOddtype username={},siteId={} error={}",param.getUsername(),param.getEntity().getSiteId(),e);
			message.insert("H8 updateH8Oddtype".length(),"修改会员限红异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-h8 updateH8Oddtype",message.toString().replace("&","*"));
		}
		return result;
	}
	
	@Override
	public String loginBySingGame(LoginParam param) {
		return this.login(param);
	}

	@Override
	public H8ApiUserEntity queryUserExist(String username) {
		logger.info("h8 username = {}", username);
		String user = username.split("&")[0];
		Integer siteId = Integer.valueOf(username.split("&")[1]);
		H8ApiUserEntityExample h8ApiUserExample = new H8ApiUserEntityExample();
		Criteria createCriteria = h8ApiUserExample.createCriteria();
		createCriteria.andUsernameEqualTo(user);
		createCriteria.andSiteIdEqualTo(siteId);
		List<H8ApiUserEntity> list = h8ApiUserEntityMapper.selectByExample(h8ApiUserExample);
		return (list == null || list.size() == 0) ? null : list.get(0);
	}

	@Override
	@Transactional
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		ApiInfoEntity entity = param.getEntity();
		String username = param.getUsername();
		Map<String, Object> resultMap = null;
		try {
			String cur = StringsUtil.isNull(param.getCur()) ? SysConstants.CUR : param.getCur();
			String oddType = StringsUtil.isNull(param.getOddtype()) ? SysConstants.CUR : param.getOddtype();
			H8ApiUserEntity user = this.queryUserExist(username + "&" + entity.getSiteId());
			if (user == null) {
				//1.创建h8会员
				resultMap = this.createMemberByH8(entity, username);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					logger.info("创建h8会员");
					user = this.createMemberByLocal(entity, username, entity.getPassword(), cur, oddType);
				}
				return resultMap;
			}
		} catch (Exception e) {
			logger.info("创建会员异常 : ", e);
			return maybe("创建h8会员异常");
		}
		return success("用户已经存在");
	}

	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		StringBuilder message = new StringBuilder();
		String result = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			message.append("H8 queryStatusByBillno 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			H8TransferVo vo = new H8TransferVo(entity.getSecret(), entity.getAgent(), param.getUsername(), H8Constants.function.QUERY_ORDER_STATUS, param.getBillno());
			result = ReflectUtil.generateParam(vo);
			message.append("请求地址：").append(entity.getReportUrl()).append("\n");
			message.append("请求参数：").append(result).append("\n");
			result = StringsUtil.sendGet(entity.getReportUrl(), result);
			message.append("接口返回：").append(result).append("\n");
			XmlUtil xmlUtil = new XmlUtil(result);
			String status = xmlUtil.getSelectNodes("/response/errcode").get(0).getStringValue();
			if ("0".equals(status)) { //成功
				result = JSONUtils.map2Json(success("success!"));
			} else {
				result = JSONUtils.map2Json(failure("failure!"));
				message.insert("H8 queryStatusByBillno".length(),"查询订单状态异常！").append("\n");
				telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-h8 queryStatusByBillno",message.toString().replace("&","*"));
			}
		} catch (Exception e) {
			logger.error("h8查询订单出错 : ", e);
			message.insert("H8 queryStatusByBillno".length(),"查询订单状态异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-h8 queryStatusByBillno",message.toString().replace("&","*"));
			result = JSONUtils.map2Json(maybe("查询订单出错 : " + result));
		}
		return result;
	}

	/**
	 * H8创建会员
	 */
	private Map<String, Object> createMemberByH8(ApiInfoEntity entity, String username) {
		StringBuilder createMessage = new StringBuilder();
		StringBuilder oddMessage = new StringBuilder();
		
		String result = null;
		int max = H8Constants.MAX;
		int lim = H8Constants.LIM;
		if (H8Constants.TRY_PLAY_AGENT.equals(entity.getAgent())) {
			max = H8Constants.MAX_TEST;
			lim = H8Constants.LIM_TEST;
		}
		
		try {
			createMessage.append("H8 createMember 站点：").append(entity.getSiteId());
			createMessage.append(",会员：").append(username);
			H8TransferVo vo = new H8TransferVo(entity.getPassword(), entity.getAgent(), username, H8Constants.function.CREATE_MEMBER);
			result = ReflectUtil.generateParam(vo);
			createMessage.append("请求地址：").append(entity.getReportUrl()).append("\n");
			createMessage.append("请求参数：").append(result).append("\n");
			result = StringsUtil.sendGet(entity.getReportUrl(), result);
			createMessage.append("接口返回：").append(result).append("\n");
		} catch (Exception e) {
			logger.error("h8创建会员异常 : ", e);
			createMessage.insert("H8 createMember".length(),"创建会员异常！").append("\n");
			createMessage.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-h8 createMember",createMessage.toString().replace("&","*"));
			return maybe("h8创建会员异常！");
		}
		
		
		XmlUtil xml = new XmlUtil(result);
		result = xml.getSelectNodes("/response/errcode").get(0).getStringValue();
		if (!H8Constants.STATE_SUCCESS.equals(result)) {
			result = xml.getSelectNodes("/response/errtext").get(0).getStringValue();
			if ("Username exist".equalsIgnoreCase(result)) {
				return success(result);
			}
			return failure(result);
		}
		H8TransferVo updateVO = new H8TransferVo(entity.getSecret(), entity.getAgent(), username, H8Constants.function.UPDATE, max + "", lim + "", //
				H8Constants.COMTYPE_A, H8Constants.COM1 + "",//
				H8Constants.COM2 + "", H8Constants.COM3 + "", H8Constants.SUSPEND + "");
		try {
			oddMessage.append("H8 updateOddType 站点：").append(entity.getSiteId());
			oddMessage.append(",会员：").append(username);
			result = ReflectUtil.generateParam(updateVO);
			oddMessage.append("请求地址：").append(entity.getReportUrl()).append("\n");
			oddMessage.append("请求参数：").append(result).append("\n");
			result = StringsUtil.sendGet(entity.getReportUrl(), result);
			oddMessage.append("接口返回：").append(result).append("\n");
			logger.info("H8 修改会员下注限额 {}", result);
		} catch (Exception e) {
			logger.error("h8创建会员更新限红异常 : ", e);
			createMessage.insert("H8 updateOddType".length(),"修改限红异常！").append("\n");
			createMessage.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-h8 updateOddType",createMessage.toString().replace("&","*"));
			return maybe("h8创建会员更新限红异常2");
		}
		return success("成功");
	}

	/**
	 * 本地创建会员
	 */
	private H8ApiUserEntity createMemberByLocal(ApiInfoEntity entity, String username, String password, String cur, String oddtype) {
		H8ApiUserEntity h8InsertUser = new H8ApiUserEntity();
		h8InsertUser.setUsername(username);
		h8InsertUser.setPassword(password);
		h8InsertUser.setAgentName(entity.getAgent());
		h8InsertUser.setApiInfoId(entity.getId().intValue());//api 的 id
		h8InsertUser.setSiteName(entity.getProjectAgent());
		h8InsertUser.setOddtype(oddtype);
		h8InsertUser.setCurrencyType(cur);
		h8InsertUser.setCreateTime(DateUtil.getCurrentTime());//创建时间
		h8InsertUser.setUserStatus(1);//状态=1 有效会员
		h8InsertUser.setSiteId(entity.getSiteId());
		h8InsertUser.setSiteName(entity.getProjectAgent());

		this.h8ApiUserEntityMapper.insert(h8InsertUser);//插入 h8 会员资料
		return h8InsertUser;
	}

	@Override
	public String queryAgentBalance(QueryBalanceParam param) {
		Map<String, Object> resultMap = null;
		double balance = 0.00;  
		try {
			String agent = param.getAgent();
			String siteId = String.valueOf(param.getEntity().getSiteId());
			logger.info("h8 查询redis缓存代理总额度 agent{}, siteId{}",agent,siteId);
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
			resultMap.put("balance",String.valueOf(balance));
			return JSONUtils.map2Json(resultMap);
		} catch (Exception e) {
			logger.error("查询代理余额异常 : ", e);
			resultMap = maybe("查询余额异常");
		}
		return JSONUtils.map2Json(resultMap);
	}
}
