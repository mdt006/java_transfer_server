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
import com.ds.transfer.common.util.XmlUtil;
import com.ds.transfer.common.vo.LoginParam;
import com.ds.transfer.common.vo.QueryBalanceParam;
import com.ds.transfer.common.vo.QueryOrderStatusParam;
import com.ds.transfer.common.vo.TransferParam;
import com.ds.transfer.common.vo.UserParam;
import com.ds.transfer.http.constants.RemarkConstants;
import com.ds.transfer.http.dao.TransferRecordDao;
import com.ds.transfer.http.service.H8TransferService;
import com.ds.transfer.http.vo.ds.TotalBalanceParam;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.H8ApiUserEntity;

@Service("h8TransferServiceImpl")
public class H8TransferServiceImpl extends CommonTransferService implements H8TransferService<H8ApiUserEntity> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource(name = "h8Transfer")
	private TransferService<H8ApiUserEntity> h8Transfer;

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
			billno = param.getBillno();
			String type = param.getType();
			remarks = RemarkConstants.H8_TRANSFER_REMARK.split(",");
			
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
				param.setLiveId(SysConstants.LiveId.H8);
				result = this.moneyCenter.transfer(param);
				providerIp = printProviderIp();
				logger.info("money transfer ip = {}, result = {}", providerIp, result);
				resultMap = JSONUtils.json2Map(result);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					param.setType(type);
					param.setBillno(billno);
					result = this.h8Transfer.transfer(param);
					providerIp = printProviderIp();
					logger.info("h8 transfer ip = {}, result = {}", providerIp, result);
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
				result = this.h8Transfer.transfer(param);
				providerIp = printProviderIp();
				logger.info("h8 transfer ip = {}, result = {}", providerIp, result);
				resultMap = JSONUtils.json2Map(result);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					param.setType(oppositeType);
					param.setLiveId(SysConstants.LiveId.H8);
					param.setBillno(billno);
					result = this.moneyCenter.transfer(param);
					providerIp = printProviderIp();
					logger.info("money transfer ip = {}, result = {}", providerIp, result);
				}
			}
			return result;
		} catch (Exception e) {
			logger.error("转账业务异常 : ", e);
			resultMap = maybe("h8转账异常");
		}
		return JSONUtils.map2Json(resultMap);
	}

	@Override
	public String queryBalance(QueryBalanceParam param) {
		Map<String, Object> resultMap = null;
		String result = null;
		String providerIp = null;
		try {
			//1.用户存在?
			ApiInfoEntity entity = param.getEntity();
			UserParam userParam = new UserParam(entity, param.getUsername(), null, null, null,null);
			resultMap = this.checkAndCreateMember(userParam);
			if (!SUCCESS.equals(resultMap.get(STATUS))) {
				return JSONUtils.map2Json(resultMap);
			}
			//2.查询余额
			result = this.h8Transfer.queryBalance(param);
			providerIp = printProviderIp();
			logger.info("h8 queryBalance ip = {},siteId={},username={},result = {}", providerIp,entity.getSiteId(),param.getUsername(),result);
			return result;
		} catch (Exception e) {
			logger.error("h8查询余额异常 : ", e);
			return JSONUtils.map2Json(maybe("h8查询余额异常"));
		}
	}

	@Override
	public String login(LoginParam param) {
		ApiInfoEntity entity = param.getEntity();
		Map<String, Object> resultMap = null;
		String result = null;
		String providerIp = null;
		try {
			//1.用户存在? 
			UserParam userParam = new UserParam(entity, param.getUsername(), null, null, "1",null);
			resultMap = this.checkAndCreateMember(userParam);
			if (!SUCCESS.equals(resultMap.get(STATUS))) {
				return JSONUtils.map2Json(resultMap);
			}
			//2.登录
			result = this.h8Transfer.login(param);
			providerIp = printProviderIp();
			logger.info("h8 login ip = {}, result = {}", providerIp, result);
			return result;
		} catch (Exception e) {
			logger.error("h8登录异常", e);
			return JSONUtils.map2Json(maybe("h8登录异常"));
		}
	}

	@Override
	public String loginBySingGame(LoginParam param) {
		return this.login(param);
	}
	@Override
	public H8ApiUserEntity queryUserExist(String username) {
		return this.h8Transfer.queryUserExist(username);
	}
	@Override
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		Map<String, Object> resultMap = this.h8Transfer.checkAndCreateMember(param);
		String providerIp = printProviderIp();
		logger.info("h8 checkAndCreateMember ip = {}, result = {}", providerIp, JSONUtils.map2Json(resultMap));
		return resultMap;
	}
	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		return this.h8Transfer.queryStatusByBillno(param);
	}

	@Override
	public String totalBalanceBySiteId(TotalBalanceParam param) {
		logger.info("h8统计余额参数 : siteId = {}, liveType = {}, fromDate = {}, toDate = {}", param.getSiteId(), param.getLiveType(), param.getFromDate(), param.getToDate());
		Double balance = 0.00;
		try {
			String agent = this.transferRecordDao.totalBalanceByAgent(param);
			if (!StringsUtil.isNull(agent)) {
				logger.info("查询到的h8代理 = {}",agent);
				Map<String, Double> resultMap = queryBalanceMultipartByUserName(param, agent);
				logger.info("h8查询代理 = {},{}",agent,resultMap.get("balance"));
				balance = resultMap.get("balance");
			}
		} catch (Exception e) {
			logger.error("h8统计余额 系统错误 :　", e);
			return JSONUtils.map2Json(failure("failure!"));
		}
		return JSONUtils.map2Json(success(balance + ""));
	}


	/**
	 * 根据用户名统计余额
	 * @param param
	 * @param userNameList
	 * @param pool
	 * @param latch
	 * @return
	 * @throws InterruptedException
	 */
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
			Map<String, Object> resultData = JSONUtils.json2Map(h8Transfer.queryAgentBalance(queryBalanceParam));
			
			if ("10000".equals(resultData.get("status"))) { //成功
				balance = Double.valueOf((String)resultData.get("balance"));
				resultMap.put("balance", balance);
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
	public String changeOddType(ApiInfoEntity entity, String username, Integer maxCreditPerBet, Integer maxCreditPerMatch) {
		String result = null;
		String params = "secret=" + entity.getSecret() + "&action=update&agent=" + entity.getAgent() + "&username=" + username + "&max1="//
				+ maxCreditPerBet + "&lim1=" + maxCreditPerMatch + "&comtype=A&com1=0&com2=0&com3=0&suspend=0";
		try {
			result = StringsUtil.sendGet(entity.getReportUrl(), params);
			XmlUtil xmlUtil = new XmlUtil(result);
			String code = xmlUtil.getSelectNodes("/response/errcode").get(0).getStringValue();
			if ("0".equals(code)) {
				return JSONUtils.map2Json(success("success!"));
			}
			return JSONUtils.map2Json(failure(xmlUtil.getSelectNodes("/response/errtext").get(0).getStringValue()));
		} catch (Exception e) {
			logger.error("H8变更限红异常 : ", e);
			return JSONUtils.map2Json(maybe("H8变更限红异常"));
		}
	}

	@Override
	public String queryAgentBalance(QueryBalanceParam queryBalanceParam) {
		return null;
	}
}
