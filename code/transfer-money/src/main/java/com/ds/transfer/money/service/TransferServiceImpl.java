package com.ds.transfer.money.service;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import com.alibaba.fastjson.JSONObject;
import com.ds.msg.TelegramMessage;
import com.ds.transfer.common.constants.SysConstants;
import com.ds.transfer.common.service.CommonTransferService;
import com.ds.transfer.common.service.TransferService;
import com.ds.transfer.common.util.JSONUtils;
import com.ds.transfer.common.util.ReflectUtil;
import com.ds.transfer.common.util.StringsUtil;
import com.ds.transfer.common.vo.LoginParam;
import com.ds.transfer.common.vo.QueryBalanceParam;
import com.ds.transfer.common.vo.QueryOrderStatusParam;
import com.ds.transfer.common.vo.TransferParam;
import com.ds.transfer.common.vo.UserParam;
import com.ds.transfer.money.constants.RemarkConstants;
import com.ds.transfer.money.constants.TelegramConstants;
import com.ds.transfer.money.entity.TransferMoneyKeyEntity;
import com.ds.transfer.money.entity.TransferMoneyKeyEntityExample;
import com.ds.transfer.money.mapper.TransferMoneyKeyEntityMapper;
import com.ds.transfer.money.util.MoneyConstants;
import com.ds.transfer.money.util.PropsUtil;
import com.ds.transfer.money.vo.LoginVo;
import com.ds.transfer.money.vo.MoneyVo;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.DsApiUserEntity;
import com.ds.transfer.record.entity.TransferRecordEntity;
import com.ds.transfer.record.entity.TransferRemarkConfEntity;
import com.ds.transfer.record.entity.TransferRemarkConfEntityExample;
import com.ds.transfer.record.mapper.TransferRemarkConfEntityMapper;
import com.ds.transfer.record.service.TransferRecordService;

public class TransferServiceImpl extends CommonTransferService implements TransferService<DsApiUserEntity> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;

	@Resource
	private TransferMoneyKeyEntityMapper transferMoneyKeyEntityMapper;
	
	@Autowired
	private TransferRemarkConfEntityMapper transferRemarkMapper;

	private Map<String, Integer> fromKeyTypeMap = new ConcurrentHashMap<String, Integer>();
	
	//小飞机消息组件
	static TelegramMessage telegramMessage = TelegramMessage.getInstance();
	
	@Override
	public String transfer(TransferParam transferParam) {
		StringBuilder message = new StringBuilder();
		ApiInfoEntity entity = transferParam.getEntity();
		
		TransferRecordEntity moneyCenterRecord = new TransferRecordEntity();//钱包中心 转账记录
		message.append("money Transfer 站点：").append(entity.getSiteId());
		message.append(",会员：").append(transferParam.getUsername());
		String type = null;
		String msg = "";
		String [] remarks = null;
		
		try {
			String billno = transferParam.getBillno();
			type = transferParam.getType();
			remarks = RemarkConstants.MONEY_REMARK.split(",");
			
			message.append(",单号：").append(billno).append("交易类型：").append(type).append("\n");
			moneyCenterRecord = this.transferRecordService.insert(entity.getSiteId(), entity.getLiveId(), transferParam.getTransRecordId(), entity.getPassword(), transferParam.getUsername(), transferParam.getCredit(), //
					billno, type, MoneyConstants.MONEY_CENTER, transferParam.getRemark(), moneyCenterRecord);
			billno += type;//编号+IN|OUT
			//查询出跟钱包中心交易的key(fromKeyType)
			Integer fromKeyType = getFromKeyType(type, entity);
			logger.info("钱包中心 转账记录插入成功, id = {}, fromKeyType = {}", moneyCenterRecord.getId(), fromKeyType);
			if (fromKeyType == null) {
				return JSONUtils.map2Json(failure("fromKeyType is not exists!"));
			}
			//钱包中心------------
			String fromKey = PropsUtil.getProperty("fromKey");
			String key = this.generateMoneyKey(entity, transferParam.getUsername(), billno, fromKey);
			MoneyVo money = new MoneyVo(fromKey, entity.getSiteId(), //
					billno, transferParam.getUsername(), type, transferParam.getCredit(), fromKeyType, transferParam.getRemark(), key);
			String param = ReflectUtil.generateParam(money);
			String url = entity.getCenterMoneyUrl() + MoneyConstants.Function.TRANS_MONEY;
			message.append("请求地址：").append(url).append("\n");
			message.append("请求参数：").append(param).append("\n");
			String result = StringsUtil.sendPost1(url, param);
			message.append("接口返回：").append(result).append("\n");
			JSONObject jsonMap = JSONObject.parseObject(result);
			msg = jsonMap.getString("message");

			String code = jsonMap.getString("code");
			//转账成功
			if (MoneyConstants.TRANS_SUCCESS.equals(code)) {
				logger.info("钱包中心转账成功...,type = {}", type);
				//更新转账信息
				moneyCenterRecord = this.transferRecordService.update(SysConstants.Record.TRANS_SUCCESS, remarks[0], moneyCenterRecord);
				return JSONUtils.map2Json(success("transfer success!"));
			}
			logger.info("钱包中心 billno={},转账失败",billno);
			
			if("110012".equals(code)){
				moneyCenterRecord = this.transferRecordService.update(SysConstants.Record.TRANS_FAILURE, remarks[1], moneyCenterRecord);
			}else{
				moneyCenterRecord = this.transferRecordService.update(SysConstants.Record.TRANS_FAILURE, remarks[2] + (IN.equals(type) ? "需人工处理" : ""), moneyCenterRecord);
			}
			
			//余额不足 | 用户金额异常 | 用户账号异常
			if ("110012".equals(code) || "110002".equals(code) || "110006".equals(code) || "110015".equals(code)) {
				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(STATUS, code);
				resultMap.put(MESSAGE, msg);
				return JSONUtils.map2Json(resultMap);
			}
			return JSONUtils.map2Json(failure(msg));
		} catch (Exception e) {
			logger.error("钱包中心 转账异常 : ", e);
			moneyCenterRecord = this.transferRecordService.update(SysConstants.Record.TRANS_MAYBE, remarks[3] + (IN.equals(type) ? "需人工处理" : ""), moneyCenterRecord);
			message.insert("money Transfer".length(),"钱包交易异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-money transfer",message.toString().replace("&", "*"));
			return JSONUtils.map2Json(maybe("system error!"));
		}
	}

	private Integer getFromKeyType(String type, ApiInfoEntity entity) {
		logger.info("fromKeyTypeMap="+fromKeyTypeMap.toString());
		return fromKeyTypeMap.get(type + entity.getLiveId());
	}

	@Override
	public String queryBalance(QueryBalanceParam param) {
		StringBuilder message = new StringBuilder();
		String product = null;
		String msg = null;
		try {
			product = RemarkConstants.PRODUCT;
			String fromKey = PropsUtil.getProperty("fromKey");
			ApiInfoEntity entity = param.getEntity();
			
			message.append("money queryBalance 站点：").append(entity.getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			String key = this.generateMoneyKey(entity, param.getUsername(), null, fromKey);
			MoneyVo vo = new MoneyVo(fromKey, entity.getSiteId(), param.getUsername(), key);
			String result = ReflectUtil.generateParam(vo);
			String url = entity.getCenterMoneyUrl() + MoneyConstants.Function.QUERY_BALANCE;
			message.append("请求地址：").append(url).append("\n");
			message.append("请求参数：").append(result).append("\n");
			result = StringsUtil.sendPost1(url, result);
			message.append("接口返回：").append(result).append("\n");
			JSONObject jsonMap = JSONObject.parseObject(result);
			String code = jsonMap.getString("code");
			msg = jsonMap.getString("message");
			if ("100000".equals(code)) {
				String balance = jsonMap.getJSONObject("data").getString("money");
				Map<String, Object> map = success(balance);
				map.put("balance", balance);
				return JSONUtils.map2Json(map);
			}
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(STATUS, code);
			resultMap.put(MESSAGE, msg);
			return JSONUtils.map2Json(resultMap);
		} catch (Exception e) {
			logger.error(product+"主账户查询余额出错", e);
			message.insert("money queryBalance".length(),"查询余额异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-money queryBalance",message.toString().replace("&", "*"));
			return JSONUtils.map2Json(maybe("system error !" + msg));
		}
	}

	@Override
	public String login(LoginParam param) {
		String result = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			String username = param.getUsername();
			String gameType = param.getGameType();
			LoginVo vo = new LoginVo(entity.getAgent(), MoneyConstants.LOGIN_CMD);
			LoginVo.Param params = null;
			if (gameType.startsWith("LOTT")) {//彩票登录
				params = vo.new Param(username, entity.getPassword(), StringsUtil.isNull(param.getCur()) ? entity.getCurrencyType() : param.getCur(), username, param.getLanguage(), Integer.valueOf(param.getLine()), //
						gameType, param.getLottoTray(), username, param.getLottoType(), "LOTTO".equalsIgnoreCase(gameType));
			} else {//大厅登录
				params = vo.new Param(username, entity.getPassword(), StringsUtil.isNull(param.getCur()) ? entity.getCurrencyType() : param.getCur(), username, param.getLanguage(), Integer.valueOf(param.getLine()), username);
			}
			vo.setParams(params);
			String loginParam = JSONUtils.bean2Json(vo);
			String url = entity.getReportUrl();
			result = StringsUtil.sendPost1(url, loginParam);
			JSONObject jsonMap = JSONObject.parseObject(result);
			if ("0".equals(jsonMap.getString("errorCode"))) {
				result = jsonMap.getJSONObject("params").getString("link");
				return JSONUtils.map2Json(success(result));
			}
		} catch (Exception e) {
			logger.error("钱包中心登录错误 : ", e);
			return JSONUtils.map2Json(maybe("system error : " + result));
		}
		return JSONUtils.map2Json(failure(result));
	}

	@Override
	public String loginBySingGame(LoginParam param) {
		return this.login(param);
	}

	@Override
	@Deprecated
	public DsApiUserEntity queryUserExist(String username) {
		return null;
	}

	/**
	 * 生成钱包中心key
	 */
	private String generateMoneyKey(ApiInfoEntity entity, String username, String billno, String fromKey) {
		StringBuilder md5Builder = new StringBuilder();
		md5Builder.append(fromKey).append(username);
		if (!StringsUtil.isNull(billno)) {
			md5Builder.append(billno);
		}
		return StringsUtil.randomString(5) + StringsUtil.toMD5(md5Builder.toString()) + StringsUtil.randomString(6);
	}

	@Override
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		return null;
	}

	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		String result = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			String fromKey = PropsUtil.getProperty("fromKey");
			MoneyVo vo = new MoneyVo(fromKey, entity.getSiteId(), param.getBillno(), param.getUsername(), //
					this.generateMoneyKey(entity, param.getUsername(), param.getBillno(), fromKey));
			result = ReflectUtil.generateParam(vo);
			String checkUrl = entity.getCenterMoneyUrl() + MoneyConstants.Function.CHECK_TRANS_MONEY;
			result = StringsUtil.sendPost1(checkUrl, result);
			JSONObject jsonMap = JSONObject.parseObject(result);
			if ("100000".equals(jsonMap.getString("code"))) {
				if ("true".equals(jsonMap.getJSONObject("data").getString("status"))) {
					return JSONUtils.map2Json(success("success!"));
				}
				result = JSONUtils.map2Json(failure("statis is failure:" + result));
			} else {
				result = JSONUtils.map2Json(maybe("static  is exception: " + result));
			}
		} catch (Exception e) {
			logger.error("查询转账异常 : ", e);
			result = JSONUtils.map2Json(maybe("query transfer is exception : " + result));
		}
		return result;
	}

	@Override
	public String queryAgentBalance(QueryBalanceParam param) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	@PostConstruct
	@Scheduled(cron = "${schedule.fromKeyType}")
	public void initData() {
		logger.info("刷新fromKeyTypeMap缓存");
		TransferMoneyKeyEntityExample example = new TransferMoneyKeyEntityExample();
		List<TransferMoneyKeyEntity> list = this.transferMoneyKeyEntityMapper.selectByExample(example);
		Integer fromKeyType = null;
		for (TransferMoneyKeyEntity entity : list) {
			fromKeyType = fromKeyTypeMap.get(entity.getType() + entity.getLiveId());
			if (fromKeyType == null) {
				fromKeyTypeMap.put(entity.getType() + entity.getLiveId(), entity.getFromKeyType());
			} else {
				if (!fromKeyType.equals(entity.getFromKeyType())) {
					fromKeyTypeMap.put(entity.getType() + entity.getLiveId(), entity.getFromKeyType());
				}
			}
		}
	}
	
	@PostConstruct
	@Scheduled(cron = "${spring.schedule}")
	public void initTransferRemark() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		try {
			TransferRemarkConfEntityExample transferRemark = new TransferRemarkConfEntityExample();
			transferRemark.createCriteria().andProjectEqualTo("transfer-money");
			List<TransferRemarkConfEntity> transferRemarkList = transferRemarkMapper.selectByExample(transferRemark);
			for (Iterator<TransferRemarkConfEntity> iterator = transferRemarkList.iterator(); iterator.hasNext();) {
				TransferRemarkConfEntity transferRemarkConfEntity = (TransferRemarkConfEntity) iterator.next();
				Field[] fields = RemarkConstants.class.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					fields[i].setAccessible(true);
					if(fields[i].getName().equals(transferRemarkConfEntity.getRemarkField())){
						fields[i].set(RemarkConstants.class, transferRemarkConfEntity.getContent());
					}
				}
			}
			logger.info("初始化钱包业务组件备注完成！");
		} catch (Exception e) {
			logger.info("初始化钱包业务组件备注异常！");
			e.printStackTrace();
		}
	}
}
