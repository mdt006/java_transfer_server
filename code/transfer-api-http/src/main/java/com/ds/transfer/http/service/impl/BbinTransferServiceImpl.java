package com.ds.transfer.http.service.impl;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ds.transfer.common.constants.SysConstants;
import com.ds.transfer.common.service.CommonTransferService;
import com.ds.transfer.common.service.TransferService;
import com.ds.transfer.common.util.JSONUtils;
import com.ds.transfer.common.util.StringsUtil;
import com.ds.transfer.common.vo.LoginParam;
import com.ds.transfer.common.vo.QueryBalanceParam;
import com.ds.transfer.common.vo.QueryOrderStatusParam;
import com.ds.transfer.common.vo.TransferParam;
import com.ds.transfer.common.vo.UserParam;
import com.ds.transfer.http.constants.RemarkConstants;
import com.ds.transfer.http.dao.TransferRecordDao;
import com.ds.transfer.http.service.BbinTransferService;
import com.ds.transfer.http.vo.ds.TotalBalanceParam;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.BbinApiUserEntity;

@Service("bbinTransferServiceImpl")
public class BbinTransferServiceImpl extends CommonTransferService implements BbinTransferService<BbinApiUserEntity> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource(name = "bbinTransfer")
	private TransferService<BbinApiUserEntity> bbinTransfer;

	@Resource(name = "moneyCenter")
	private TransferService<?> moneyCenter;
	
	@Autowired
	private TransferRecordDao transferRecordDao;

	@Override
	public String transfer(TransferParam param) {
		String transferRemark = null;
		String oppositeType = null;
		String result = null;
		String billno = null;
		String providerIp = null;
		Map<String, Object> resultMap = null;
		String[] remarks = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			String type = param.getType();
			billno = param.getBillno();
			remarks = RemarkConstants.BBIN_TRANSFER_REMARK.split(",");
			
			if (IN.equals(type)) {
				if (param.isTotalBalance()) {
					transferRemark = remarks[0];
					param.setBillno(billno + "T" + "_" + entity.getLiveName());
				} else {
					transferRemark = remarks[1] + (StringsUtil.isNull(param.getRemark()) ? "" : param.getRemark());
				}
				oppositeType = OUT;
				param.setType(oppositeType);
				param.setRemark(transferRemark);
				param.setLiveId(SysConstants.LiveId.BBIN);
				result = this.moneyCenter.transfer(param);
				providerIp = printProviderIp();
				logger.info("money transfer ip = {}, result = {}", providerIp, result);
				resultMap = JSONUtils.json2Map(result);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					param.setType(type);
					param.setBillno(billno);
					result = this.bbinTransfer.transfer(param);
					providerIp = printProviderIp();
					logger.info("bbin transfer ip = {}, result = {}", providerIp, result);
				}
			} else {
				if (param.isTotalBalance()) {
					transferRemark = remarks[2];
					billno = billno + "T" + "_" + entity.getLiveName();
				} else {
					transferRemark = remarks[3] + (StringsUtil.isNull(param.getRemark()) ? "" : param.getRemark());
				}
				oppositeType = IN;
				param.setType(type);
				param.setRemark(transferRemark);
				result = this.bbinTransfer.transfer(param);
				providerIp = printProviderIp();
				logger.info("bbin transfer ip = {}, result = {}", providerIp, result);
				resultMap = JSONUtils.json2Map(result);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					param.setType(oppositeType);
					param.setLiveId(SysConstants.LiveId.BBIN);
					param.setBillno(billno);
					result = this.moneyCenter.transfer(param);
					providerIp = printProviderIp();
					logger.info("money transfer ip = {}, result = {}", providerIp, result);
				}
			}
			resultMap = JSONUtils.json2Map(result);
		} catch (Exception e) {
			logger.error("money<---->bbin转账错误", e);
			return JSONUtils.map2Json(maybe("bbin转账异常"));
		}
		return JSONUtils.map2Json(resultMap);
	}

	@Override
	public String queryBalance(QueryBalanceParam param) {
		ApiInfoEntity entity = param.getEntity();
		Map<String, Object> resultMap = null;
		String result = null;
		String providerIp = null;
		try {
			//1.用户存在? 
			UserParam userParam = new UserParam(entity, param.getUsername(), null, null, null,null);
			resultMap = this.checkAndCreateMember(userParam);
			if (!SUCCESS.equals(resultMap.get(STATUS))) {
				return JSONUtils.map2Json(resultMap);
			}
			//2.查询余额
			result = this.bbinTransfer.queryBalance(param);
			providerIp = printProviderIp();
			logger.info("bbin queryBalance ip = {},siteId={},username={},result = {}", providerIp,entity.getSiteId(),param.getUsername(), result);
			return result;
		} catch (Exception e) {
			logger.error("bbin查询余额异常",e);
			return JSONUtils.map2Json(maybe("bbin查询余额异常"));
		}
	}

	@Override
	public String login(LoginParam param) {
		ApiInfoEntity entity = param.getEntity();
		String username = param.getUsername();
		String pfUserName = entity.getPrefix() + username;
		Map<String, Object> resultMap = null;
		String result = null;
		String providerIp = null;
		try {
			//1.用户存在? 
			UserParam userParam = new UserParam(entity, username, null, null, "1",param.getLoginUrl());
			resultMap = this.checkAndCreateMember(userParam);
			if (!SUCCESS.equals(resultMap.get(STATUS))) {
				return JSONUtils.map2Json(resultMap);
			}
			//2.登录
			param.setUsername(pfUserName);
			result = this.bbinTransfer.login(param);
			providerIp = printProviderIp();
			logger.info("bbin login ip = {}, result = {}", providerIp, result);
			return result;
		} catch (Exception e) {
			logger.error("bbin登录异常", e);
			return JSONUtils.map2Json(maybe("bbin登录异常"));
		}
	}

	@Override
	public String loginBySingGame(LoginParam param) {
		ApiInfoEntity entity = param.getEntity();
		Map<String, Object> resultMap = null;
		try {
			//1.用户存在? 
			UserParam userParam = new UserParam(entity, param.getUsername(), null, null, "1", param.getLoginUrl());
			resultMap = this.checkAndCreateMember(userParam);
			if (!SUCCESS.equals(resultMap.get(STATUS))) {
				return JSONUtils.map2Json(resultMap);
			}
			//2.登录
			return this.bbinTransfer.loginBySingGame(param);
		} catch (Exception e) {
			logger.error("bbin登录异常", e);
		}
		return null;
	}

	@Override
	public BbinApiUserEntity queryUserExist(String username) {
		return this.bbinTransfer.queryUserExist(username);
	}

	@Override
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		Map<String, Object> resultMap = this.bbinTransfer.checkAndCreateMember(param);
		String providerIp = printProviderIp();
		logger.info("bbin checkAndCreateMember ip = {}, result = {}", providerIp, JSONUtils.map2Json(resultMap));
		return resultMap;
	}

	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		return this.bbinTransfer.queryStatusByBillno(param);
	}
	
	@Override
	public String totalBalanceBySiteId(TotalBalanceParam param) {
		logger.info("bbin统计余额参数 : siteId = {}, liveType = {}, fromDate = {}, toDate = {}", param.getSiteId(), param.getLiveType(), param.getFromDate(), param.getToDate());
		Double balance = 0.00;
		try {
			String agent = this.transferRecordDao.totalBalanceByAgent(param);
			if (!StringsUtil.isNull(agent)) {
				logger.info("查询到的bbin代理 = {}",agent);
				Map<String, Double> resultMap = queryBalanceMultipartByUserName(param, agent);
				logger.info("bbin查询代理 = {},{}",agent,resultMap.get("balance"));
				balance = resultMap.get("balance");
			}
		} catch (Exception e) {
			logger.error("bbin统计余额 系统错误 :　", e);
			return JSONUtils.map2Json(failure("failure!"));
		}
		return JSONUtils.map2Json(success(balance + ""));
	}
	
	private Map<String, Double> queryBalanceMultipartByUserName(TotalBalanceParam param, String agent) throws Exception {
		Map<String, Double> resultMap = new HashMap<String,Double>();
		Double balance = 0.00;
		try {
			//封装请求参数
			QueryBalanceParam queryBalanceParam = new QueryBalanceParam();
			queryBalanceParam.setEntity(param.getEntity());
			queryBalanceParam.setAgent(agent);
			queryBalanceParam.setCur( param.getEntity().getCurrencyType());
			//远程调查询余额
			Map<String, Object> resultData = JSONUtils.json2Map(bbinTransfer.queryAgentBalance(queryBalanceParam));
			if ("10000".equals(resultData.get("status"))) { //成功
				resultMap.put("balance", Double.valueOf((String) resultData.get("balance")));
				return resultMap;
			} else { //失败返回0
				logger.error("username = {}, 查询金额错误!", queryBalanceParam.getUsername());
				resultMap.put("balance",balance);
				return resultMap;
			}
		} catch (Exception e) {
			logger.error("系统处理异常!",e);
			resultMap.put("balance",balance);
			return resultMap;
		}
	}

	@Override
	public String queryAgentBalance(QueryBalanceParam queryBalanceParam) {
		return null;
	}
}
