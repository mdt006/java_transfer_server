package com.ds.transfer.http.service.impl;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
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
import com.ds.transfer.http.service.PtTransferService;
import com.ds.transfer.http.vo.ds.TotalBalanceParam;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.PtApiUserEntity;

/*@Service("ptTransferServiceImpl")*/
public class PtTransferServiceImpl extends CommonTransferService implements PtTransferService<PtApiUserEntity> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource(name = "ptTransfer")
	private TransferService<PtApiUserEntity> ptTransfer;

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
			remarks = RemarkConstants.PT_TRANSFER_REMARK.split(",");
			
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
				param.setLiveId(SysConstants.LiveId.PT);
				result = this.moneyCenter.transfer(param);
				providerIp = printProviderIp();
				logger.info("money transfer ip = {}, result = {}", providerIp, result);
				resultMap = JSONUtils.json2Map(result);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					param.setType(type);
					param.setBillno(billno);
					result = this.ptTransfer.transfer(param);
					providerIp = printProviderIp();
					logger.info("PT transfer ip = {}, result = {}", providerIp, result);
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
				result = this.ptTransfer.transfer(param);
				providerIp = printProviderIp();
				logger.info("pt transfer ip = {}, result = {}", providerIp, result);
				resultMap = JSONUtils.json2Map(result);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					param.setType(oppositeType);
					param.setLiveId(SysConstants.LiveId.PT);
					param.setBillno(billno);
					result = this.moneyCenter.transfer(param);
					providerIp = printProviderIp();
					logger.info("money transfer ip = {}, result = {}", providerIp, result);
				}
			}
			resultMap = JSONUtils.json2Map(result);
		} catch (Exception e) {
			logger.error("money<---->pt转账错误", e);
			return JSONUtils.map2Json(maybe("pt转账异常"));
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
			result = this.ptTransfer.queryBalance(param);
			providerIp = printProviderIp();
			logger.info("PT queryBalance ip = {},siteId={},username={},result = {}", providerIp,entity.getSiteId(),param.getUsername(),result);
			return result;
		} catch (Exception e) {
			logger.error("PT查询余额异常",e);
			return JSONUtils.map2Json(maybe("PT查询余额异常"));
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
			
			//2.组装登陆参数
			PtApiUserEntity ptUser = (PtApiUserEntity)resultMap.get("userInfo");
			param.setUsername(ptUser.getUsername());
			param.setPassWord(ptUser.getPassword());
			
			//3.登录
			result = this.ptTransfer.login(param);
			providerIp = printProviderIp();
			logger.info("PT login ip = {}, result = {}", providerIp, result);
			return result;
		} catch (Exception e) {
			logger.error("PT登录异常", e);
			return JSONUtils.map2Json(maybe("PT登录异常"));
		}
	}

	@Override
	public String loginBySingGame(LoginParam param) {
		ApiInfoEntity entity = param.getEntity();
		Map<String, Object> resultMap = null;
		try {
			//1.用户存在? 
			UserParam userParam = new UserParam(entity, param.getUsername(), null, null, "1",null);
			resultMap = this.checkAndCreateMember(userParam);
			if (!SUCCESS.equals(resultMap.get(STATUS))) {
				return JSONUtils.map2Json(resultMap);
			}

			//2.登录
			return this.ptTransfer.loginBySingGame(param);
		} catch (Exception e) {
			logger.error("PT登录异常", e);
		}
		return null;
	}

	@Override
	public PtApiUserEntity queryUserExist(String username) {
		return this.ptTransfer.queryUserExist(username);
	}

	@Override
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		Map<String, Object> resultMap = this.ptTransfer.checkAndCreateMember(param);
		String providerIp = printProviderIp();
		logger.info("pt checkAndCreateMember ip = {}, result = {}", providerIp,JSON.toJSONString(resultMap));
		return resultMap;
	}
	


	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		return this.ptTransfer.queryStatusByBillno(param);
	}
	
	@Override
	public String totalBalanceBySiteId(TotalBalanceParam param) {
		logger.info("PT统计余额参数 : siteId = {}, liveType = {}, fromDate = {}, toDate = {}", param.getSiteId(), param.getLiveType(), param.getFromDate(), param.getToDate());
		Double balance = 0.00;
		try {
			String agent = this.transferRecordDao.totalBalanceByAgent(param);
			if (!StringsUtil.isNull(agent)) {
				logger.info("查询到的PT代理 = {}",agent);
				Map<String, Double> resultMap = queryBalanceMultipartByUserName(param, agent);
				logger.info("PT查询代理 = {},{}",agent,resultMap.get("balance"));
				balance = resultMap.get("balance");
			}
		} catch (Exception e) {
			logger.error("PT统计余额 系统错误 :　", e);
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
			Map<String, Object> resultData = JSONUtils.json2Map(ptTransfer.queryAgentBalance(queryBalanceParam));
			
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
