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
import com.ds.transfer.http.service.SgsTransferService;
import com.ds.transfer.http.vo.ds.TotalBalanceParam;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.SgsApiUserEntity;

/**
 *@ClassName: SgsTransferServiceImpl
 * @Description: TODO(SGS业务)
 * @author leo
 * @date 2018年06月06日
 */
/*@Service("sgsTransferServiceImpl")*/
public class SgsTransferServiceImpl extends CommonTransferService implements SgsTransferService<SgsApiUserEntity> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource(name = "sgsTransfer")
	private TransferService<SgsApiUserEntity> sgsTransfer;

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
		String type = null;
		String[] remarks = null;
		String product = null;
		
		try {
			type = param.getType();
			ApiInfoEntity entity = param.getEntity();
			billno = param.getBillno();
			remarks = RemarkConstants.SGS_TRANSFER_REMARK.split(",");
			product = RemarkConstants.PRODUCT;
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
				param.setLiveId(SysConstants.LiveId.SGS);
				result = this.moneyCenter.transfer(param);
				providerIp = printProviderIp();
				logger.info("money transfer ip = {}, result = {}", providerIp, result);
				resultMap = JSONUtils.json2Map(result);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					param.setType(type);
					param.setBillno(billno);
					result = this.sgsTransfer.transfer(param);
					providerIp = printProviderIp();
					logger.info("sgs transfer ip = {}, result = {}", providerIp, result);
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
				result = this.sgsTransfer.transfer(param);
				providerIp = printProviderIp();
				logger.info("sgs transfer ip = {}, result = {}", providerIp, result);
				resultMap = JSONUtils.json2Map(result);
				if (SUCCESS.equals(resultMap.get(STATUS))) {
					param.setType(oppositeType);
					param.setLiveId(SysConstants.LiveId.SGS);
					param.setBillno(billno);
					result = this.moneyCenter.transfer(param);
					providerIp = printProviderIp();
					logger.info("money transfer ip = {}, result = {}", providerIp, result);
				}
			}
			resultMap = JSONUtils.json2Map(result);
		} catch (Exception e) {
			logger.info("sgs transfer username={},siteId={},type={},error:{}!",param.getUsername(),
					param.getEntity().getSiteId(),(type.equals(IN)==true ? product+" -> SGS error!" : "SGS -> "+product+" error!"),e);
			e.printStackTrace();
			return JSONUtils.map2Json(maybe("sgs转账异常"));
		}
		return JSONUtils.map2Json(resultMap);
	}

	
	@Override
	public String queryBalance(QueryBalanceParam param) {
		String result = null;
		Map<String, Object> createMemberResultMap = null;
		String providerIp = null;
		Integer siteId = null;
		try {
			providerIp = printProviderIp();
			ApiInfoEntity entity = param.getEntity();
			siteId = entity.getSiteId();
			
			String cur = "CNY".equals(param.getCur()) ? SysConstants.SGS_CUR : param.getCur();
			param.setCur(cur);
			//1.用户存在? 
			UserParam userParam = new UserParam(param.getUsername(),cur,param.getIsDemo(),param.getPlayerIp(),param.getTerminal(),entity);
			logger.info("sgs queryBalance-->checkAndCreateMember before username={},siteId={}",param.getUsername(),siteId);
			long startCuTime = System.currentTimeMillis();
			createMemberResultMap = this.sgsTransfer.checkAndCreateMember(userParam);
			long endCuTime = System.currentTimeMillis();
			logger.info("sgs queryBalance-->checkAndCreateMember after username={},siteId={},elapsedTime={} ms,result={}",param.getUsername(),siteId,(endCuTime-startCuTime),createMemberResultMap.toString());
			if (!SUCCESS.equals(createMemberResultMap.get(STATUS))) {
				return JSONUtils.map2Json(createMemberResultMap);
			}
			logger.info("sgs queryBalance before username={},siteId={},entity ip = {}",param.getUsername(),siteId,param.getIp());
			long startTime = System.currentTimeMillis();
			result = this.sgsTransfer.queryBalance(param);
			long endTime = System.currentTimeMillis();
			providerIp = printProviderIp();
			logger.info("sgs queryBalance after username={},siteId={},ip={},elapsedTime={} ms,result={}",param.getUsername(),siteId,providerIp,(endTime-startTime),result);
			return result;
		} catch (Exception e) {
			logger.error("sgs queryBalance username={},siteId={},ip={},error:{}!",param.getUsername(),siteId,providerIp,e);
			e.printStackTrace();
			return JSONUtils.map2Json(maybe("SGS查询余额异常"));
		}
	}

	public String login(LoginParam param) {
		String result = "";
		try {
			logger.info("sgs login before username={},siteId={}",param.getUsername(),param.getEntity().getSiteId());
			long startTime = System.currentTimeMillis();
			result = this.sgsTransfer.login(param);
			long endTime = System.currentTimeMillis();
			String providerIp = printProviderIp();
			logger.info("sgs login after username={},siteId={},ip={},elapsedTime={},result={}",
					param.getUsername(),param.getEntity().getSiteId(),providerIp,(endTime-startTime),result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String totalBalanceBySiteId(TotalBalanceParam param) {
		logger.info("sgs totalBalanceBySiteId siteId={},liveType={},fromDate={},toDate={}",param.getSiteId(), param.getLiveType(), param.getFromDate(), param.getToDate());
		Double balance = 0.00;
		try {
			String agent = this.transferRecordDao.totalBalanceByAgent(param);
			if (!StringsUtil.isNull(agent)) {
				logger.info("sgs queryTotal agent={}",agent);
				long startTime = System.currentTimeMillis();
				Map<String, Double> resultMap = this.queryBalanceMultipartByAgent(param, agent);
				long endTime = System.currentTimeMillis();
				balance = resultMap.get("balance");
				logger.info("sgs queryTotal agent={},totalBalance={},elapsedTime={} ms",agent,balance,(endTime-startTime));
			}
		} catch (Exception e) {
			logger.info("sgs totalBalanceBySiteId siteId={},fromDate={},toDate={},error:{}!",param.getSiteId(),param.getFromDate(), param.getToDate(),e);
			e.printStackTrace();
			return JSONUtils.map2Json(failure("failure!"));
		}
		return JSONUtils.map2Json(success(String.valueOf(balance)));
	}

	private Map<String, Double> queryBalanceMultipartByAgent(TotalBalanceParam param, String agent) {
		Map<String, Double> resultMap = new HashMap<String,Double>();
		Double balance = 0.00;
		try {
			QueryBalanceParam queryBalanceParam = new QueryBalanceParam();
			queryBalanceParam.setEntity(param.getEntity());
			queryBalanceParam.setAgent(agent);
			queryBalanceParam.setCur( param.getEntity().getCurrencyType());
			Map<String, Object> resultData = JSONUtils.json2Map(sgsTransfer.queryAgentBalance(queryBalanceParam));
			if ("10000".equals(resultData.get("status"))) { //成功
				resultMap.put("balance", Double.valueOf((String) resultData.get("balance")));
				return resultMap;
			} else { //失败返回0
				logger.error("sgs agentTotalBalance siteId={},agent={} error!",agent,param.getSiteId());
				resultMap.put("balance",balance);
				return resultMap;
			}
		} catch (Exception e) {
			logger.error("sgs agentTotalBalance siteId={},agent={} error:{}!",agent,param.getSiteId(),e);
			e.printStackTrace();
			resultMap.put("balance",balance);
			return resultMap;
		}
	}
	
	@Override
	public SgsApiUserEntity queryUserExist(String username) {
		return this.sgsTransfer.queryUserExist(username);
	}

	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		return this.sgsTransfer.queryStatusByBillno(param);
	}

	@Override
	public String loginBySingGame(LoginParam param) {
		return this.login(param);
	}
	@Override
	public String queryAgentBalance(QueryBalanceParam queryBalanceParam) {
		return null;
	}
	@Override
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		return null;
	}
}
