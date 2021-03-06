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
import com.ds.transfer.http.service.MgTransferService;
import com.ds.transfer.http.vo.ds.TotalBalanceParam;
import com.ds.transfer.record.entity.MgApiUserEntity;
import com.ds.transfer.record.entity.ApiInfoEntity;

/*@Service("mgTransferServiceImpl")*/
public class MgTransferServiceImpl extends CommonTransferService implements MgTransferService<MgApiUserEntity> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource(name = "mgTransfer")
	private TransferService<MgApiUserEntity> mgTransfer;

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
			remarks = RemarkConstants.MG_TRANSFER_REMARK.split(",");
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
				param.setLiveId(SysConstants.LiveId.MG);
				result = this.moneyCenter.transfer(param);
				providerIp = printProviderIp();
				logger.info("money transfer ip = {}, result = {}", providerIp, result);
				resultMap = JSONUtils.json2Map(result);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					param.setType(type);
					param.setBillno(billno);
					result = this.mgTransfer.transfer(param);
					providerIp = printProviderIp();
					logger.info("mg transfer ip = {}, result = {}", providerIp, result);
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
				result = this.mgTransfer.transfer(param);
				providerIp = printProviderIp();
				logger.info("mg transfer ip = {}, result = {}", providerIp, result);
				resultMap = JSONUtils.json2Map(result);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					param.setType(oppositeType);
					param.setLiveId(SysConstants.LiveId.MG);
					param.setBillno(billno);
					result = this.moneyCenter.transfer(param);
					providerIp = printProviderIp();
					logger.info("money transfer ip = {}, result = {}", providerIp, result);
				}
			}
			resultMap = JSONUtils.json2Map(result);
		} catch (Exception e) {
			logger.error("money<---->mg转账错误", e);
			return JSONUtils.map2Json(maybe("mg转账异常"));
		}
		return JSONUtils.map2Json(resultMap);
	}

	@Override
	public String queryBalance(QueryBalanceParam param) {
		Map<String, Object> resultMap = null;
		String result = null;
		String providerIp = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			//1.用户存在? 
			UserParam userParam = new UserParam(entity, param.getUsername(), null, null, null,null);
			resultMap = this.checkAndCreateMember(userParam);
			if (!SUCCESS.equals(resultMap.get(STATUS))) {
				return JSONUtils.map2Json(resultMap);
			}
			//2.查询余额
			providerIp = printProviderIp();
			logger.info("mg queryBalance entity ip = {}",param.getIp());
			result = this.mgTransfer.queryBalance(param);
			providerIp = printProviderIp();
			logger.info("mg queryBalance ip = {},siteId={},username={},result = {}", providerIp,entity.getSiteId(),param.getUsername(),result);
			return result;
		} catch (Exception e) {
			logger.error("MG查询余额异常 : ", e);
			return JSONUtils.map2Json(maybe("MG查询余额异常"));
		}
	}

	@Override
	public String login(LoginParam param) {
		Map<String, Object> resultMap = null;
		String result = null;
		String providerIp = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			//1.用户存在? 
			UserParam userParam = new UserParam(entity, param.getUsername(), null, null, null,null);
			resultMap = this.checkAndCreateMember(userParam);
			if (!SUCCESS.equals(resultMap.get(STATUS))) {
				return JSONUtils.map2Json(resultMap);
			}
			//2.登录
			result = this.mgTransfer.login(param);
			providerIp = printProviderIp();
			logger.info("mg login ip = {}, result = {}", providerIp, result);
			return result;
		} catch (Exception e) {
			logger.error("mg登录异常 : ", e);
			return JSONUtils.map2Json(maybe("mg登录异常"));
		}
	}

	@Override
	public String loginBySingGame(LoginParam param) {
		return this.login(param);
	}

	@Override
	public MgApiUserEntity queryUserExist(String username) {
		return this.mgTransfer.queryUserExist(username);
	}

	@Override
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		Map<String, Object> resultMap = this.mgTransfer.checkAndCreateMember(param);
		String providerIp = printProviderIp();
		logger.info("mg checkAndCreateMember ip = {}, result = {}", providerIp, JSONUtils.map2Json(resultMap));
		return resultMap;
	}

	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		return this.mgTransfer.queryStatusByBillno(param);
	}

	@Override
	public String totalBalanceBySiteId(TotalBalanceParam param) {
		logger.info("MG统计余额参数 : siteId = {}, liveType = {}, fromDate = {}, toDate = {}", param.getSiteId(), param.getLiveType(), param.getFromDate(), param.getToDate());
		Double balance = 0.00;
		try {
			String agent = this.transferRecordDao.totalBalanceByAgent(param);
			if (!StringsUtil.isNull(agent)) {
				logger.info("查询到的MG代理 = {}",agent);
				Map<String, Double> resultMap = queryBalanceMultipartByUserName(param, agent);
				logger.info("MG查询代理 = {},{}",agent,resultMap.get("balance"));
				balance = resultMap.get("balance");
			}
		} catch (Exception e) {
			logger.error("MG统计余额 系统错误 :　", e);
			return JSONUtils.map2Json(failure("failure!"));
		}
		return JSONUtils.map2Json(success(balance + ""));
	}

	private Map<String, Double> queryBalanceMultipartByUserName(TotalBalanceParam param, String agent) {
		Map<String, Double> resultMap = new HashMap<String,Double>();
		Double balance = 0.00;
		try {
			//封装请求参数
			QueryBalanceParam queryBalanceParam = new QueryBalanceParam();
			queryBalanceParam.setEntity(param.getEntity());
			queryBalanceParam.setAgent(agent);
			queryBalanceParam.setCur( param.getEntity().getCurrencyType());
			//远程调查询余额
			Map<String, Object> resultData = JSONUtils.json2Map(mgTransfer.queryAgentBalance(queryBalanceParam));
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
