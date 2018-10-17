package com.ds.transfer.http.controller;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.ds.transfer.common.constants.SysConstants;
import com.ds.transfer.common.service.TransferService;
import com.ds.transfer.common.util.JSONUtils;
import com.ds.transfer.common.util.StringsUtil;
import com.ds.transfer.common.vo.LoginParam;
import com.ds.transfer.common.vo.QueryBalanceParam;
import com.ds.transfer.common.vo.QueryOrderStatusParam;
import com.ds.transfer.common.vo.TransferParam;
import com.ds.transfer.http.constants.ApplicationContstants;
import com.ds.transfer.http.constants.TransferConstants;
import com.ds.transfer.http.constants.TransferMoneyConstants;
import com.ds.transfer.http.entity.BalanceTotalEntity;
import com.ds.transfer.http.entity.LimitAmountInfo;
import com.ds.transfer.http.entity.TransferRecordDetailEntity;
import com.ds.transfer.http.entity.TransferStatus;
import com.ds.transfer.http.service.AgTransferService;
import com.ds.transfer.http.service.ApiInfoService;
import com.ds.transfer.http.service.BbinTransferService;
import com.ds.transfer.http.service.DsTransferService;
import com.ds.transfer.http.service.KkwTransferService;
import com.ds.transfer.http.service.FenfenTransferService;
import com.ds.transfer.http.service.H8TransferService;
import com.ds.transfer.http.service.KyTransferService;
import com.ds.transfer.http.service.LmgTransferService;
import com.ds.transfer.http.service.MgTransferService;
import com.ds.transfer.http.service.OgTransferService;
import com.ds.transfer.http.service.PlatformUrlService;
import com.ds.transfer.http.service.PtTransferService;
import com.ds.transfer.http.service.SgsTransferService;
import com.ds.transfer.http.service.SupportTransferService;
import com.ds.transfer.http.service.TransRecordService;
import com.ds.transfer.http.service.TransferRecordDetailService;
import com.ds.transfer.http.service.XiaoyuTransferService;
import com.ds.transfer.http.util.PropsUtil;
import com.ds.transfer.http.vo.ds.TotalBalanceParam;
import com.ds.transfer.http.vo.thread.TotalBalanceByLive;
import com.ds.transfer.record.entity.AgApiUserEntity;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.BbinApiUserEntity;
import com.ds.transfer.record.entity.DsApiUserEntity;
import com.ds.transfer.record.entity.H8ApiUserEntity;
import com.ds.transfer.record.entity.KkwApiUserEntity;
import com.ds.transfer.record.entity.LmgApiUserEntity;
import com.ds.transfer.record.entity.MgApiUserEntity;
import com.ds.transfer.record.entity.OgApiUserEntity;
import com.ds.transfer.record.entity.PtApiUserEntity;
import com.ds.transfer.record.entity.SgsApiUserEntity;

/**
 * http转账接口
 * @author jackson
 */
@RestController
public class TransferControllerNew extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(TransferControllerNew.class);

	@Resource(name = "agTransferServiceImpl")
	private AgTransferService<AgApiUserEntity> agTransferService;

	@Resource(name = "bbinTransferServiceImpl")
	private BbinTransferService<BbinApiUserEntity> bbinTransferService;

	@Resource(name = "transferRecordDetailServiceImpl")
	private TransferRecordDetailService transferRecordDetailService;
	/*@Resource(name = "h8TransferServiceImpl")
	private H8TransferService<H8ApiUserEntity> h8TransferService;

	@Resource(name = "ogTransferServiceImpl")
	private OgTransferService<OgApiUserEntity> ogTransferService;

	@Resource(name = "dsTransferServiceImpl")
	private DsTransferService<DsApiUserEntity> dsTransferService;

	@Resource(name = "fenfenTransferServiceImpl")
	private FenfenTransferService<?> fenfenTransferService;

	@Resource(name = "xiaoyuTransferServiceImpl")
	private XiaoyuTransferService<?> xiaoyuTransferService;

	@Resource(name = "transferRecordDetailServiceImpl")
	private TransferRecordDetailService transferRecordDetailService;
	
	@Resource(name = "transRecordServiceImpl")
	private TransRecordService transferRecordService;

	@Resource(name = "mgTransferServiceImpl")
	private MgTransferService<MgApiUserEntity> mgTransferService;
	
	@Resource(name="ptTransferServiceImpl")
	private PtTransferService<PtApiUserEntity> ptTransferService;
	
	@Resource(name="lmgTransferServiceImpl")
	private LmgTransferService<LmgApiUserEntity> lmgTransferService;
	
	@Resource(name="kyTransferServiceImpl")
	private KyTransferService<LmgApiUserEntity> kyTransferService;
	
	@Resource(name="sgsTransferServiceImpl")
	private SgsTransferService<SgsApiUserEntity> sgsTransferService;
	
	@Resource(name="kkwTransferServiceImpl")
	private KkwTransferService<KkwApiUserEntity> kkwTransferService;*/
	
	@Resource(name = "moneyCenter")
	private TransferService<?> moneyCenterService;

	@Resource
	private ApiInfoService apiInfoService;

	@Resource(name = "platformUrlServiceImpl")
	private PlatformUrlService platformUrlService;
	
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	private final Map<String, TransferService<?>> transferServiceMap = ApplicationContstants.TRANSFER_SERVICE_MAP;
	private final Map<String, SupportTransferService<?>> supportServiceMap = ApplicationContstants.SUPPORT_SERVICE_MAP;
	
	/**
	 * 额度转换新接口
	 * @param username 用户名
	 * @param key key
	 * @param siteId  网站id
	 * @param live 视讯类别 存放AG=2 BBIN=11 DS=12 H8=13 MG=15 格式:live1-live2 example:2-11 表示 ag-bbin与transMethod 相对应
	 * @param oddtype 限红 在查询余额用户不存在,创建会员的时候用到
	 * @param billno 唯一转账编码
	 * @param type 转账类型
	 * @param credit 金额
	 * @param isDemo 是否是试玩 试玩:0,正式:1(默认,不传就是正式)
	 * @param cur 货币类型
	 * @param transMethod 转账类型 ag | bbin | h8 | ag-bbin | ag-h8 | bbin-h8 | ag-mg | balanceTotal;优先级ag,bbin,h8,mg
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "transfer", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public @ResponseBody String transfer(String username, String operator, String key, String siteId, String live,
			String oddtype,String billno, String type, String credit, String cur, String transMethod,String playerIp,String terminal, //
			HttpServletRequest request, HttpServletResponse response) {
		String ip = StringsUtil.getIpAddr(request);// 获取请求的 ip
		String isDemo = PropsUtil.getProperty("isDemo");
		logger.info("转账 : username = {}, operator = {}, key = {}, siteId = {}, live = {}, oddtype = {}, billno = {}, type = {},"
				+ " credit = {}, isDemo = {}, cur = {}, transMethod = {}, ip = {},playerIp={},terminal={}", //
				username, operator, key, siteId, live, oddtype, billno, type, credit, isDemo, cur, transMethod, ip,playerIp,terminal);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			oddtype = StringsUtil.isNull(oddtype) ? "A" : oddtype;
			cur = StringsUtil.isNull(cur) ? "CNY" : cur;
			if (StringsUtil.isNull(billno)) {
				resultMap.put(STATUS, PARAM_FORMAT_ERROR);
				resultMap.put(MESSAGE, "billno is null");
				return JSONUtils.map2Json(resultMap);
			}
			if (StringsUtil.isNull(key) && key.length() > 6) {
				resultMap.put(STATUS, PARAM_FORMAT_ERROR);
				resultMap.put(MESSAGE, "key is null or key is error");
				return JSONUtils.map2Json(resultMap);
			}
			if (StringsUtil.isNull(live)) {
				resultMap.put(STATUS, PARAM_FORMAT_ERROR);
				resultMap.put(MESSAGE, "live is null");
				return JSONUtils.map2Json(resultMap);
			}
			if (!live.equals(SysConstants.LiveId.TOTAL)) {
				if (credit == null) {
					resultMap.put(STATUS, PARAM_FORMAT_ERROR);
					resultMap.put(MESSAGE, "credit is null");
					return JSONUtils.map2Json(resultMap);
				}
				// 校验转账 限额
				String limitAmountValidate = limitAmountValidate(siteId,credit,resultMap);
				if(!StringsUtil.isNull(limitAmountValidate)){
					return limitAmountValidate;
				}
				if (Integer.valueOf(credit) <= 0) {
					resultMap.put(STATUS, PARAM_FORMAT_ERROR);
					resultMap.put(MESSAGE, "money is error, must > 0");
					return JSONUtils.map2Json(resultMap);
				}
				if (StringsUtil.isNull(type)) {
					resultMap.put(STATUS, PARAM_FORMAT_ERROR);
					resultMap.put(MESSAGE, "type is null");
					return JSONUtils.map2Json(resultMap);
				} else {
					if (!IN.equals(type) && !OUT.equals(type)) {
						resultMap.put(STATUS, PARAM_FORMAT_ERROR);
						resultMap.put(MESSAGE, "type is not in (IN , OUT)");
						return JSONUtils.map2Json(resultMap);
					}
				}
			}
			if (StringsUtil.isNull(siteId) || !StringsUtil.isNumeric(siteId)) {
				resultMap.put(STATUS, PARAM_FORMAT_ERROR);
				resultMap.put(MESSAGE, "siteId is error");
				return JSONUtils.map2Json(resultMap);
			}

			ApiInfoEntity firstEntity = null;
			ApiInfoEntity secordEntity = null;
			Map<String, ApiInfoEntity> siteIdLiveIdMap = apiInfoService.getSiteIdLiveIdMap();
			if (live.contains("_")) {
				String[] arr = live.split("_");
				String firstKey = siteId + "" + isDemo + "" + arr[0];
				String secondKey = siteId + "" + isDemo + "" + arr[1];
				firstEntity = siteIdLiveIdMap.get(firstKey);
				secordEntity = siteIdLiveIdMap.get(secondKey);
				if (firstEntity == null || secordEntity == null) {
					resultMap.put(STATUS, SITEID_OR_LIVE_NOEXIST);
					resultMap.put(MESSAGE, "siteId or live is not found in api");
					return JSONUtils.map2Json(resultMap);
				}
			} else {
				firstEntity = siteIdLiveIdMap.get(siteId + isDemo + live);
			}

			if (firstEntity == null) {
				resultMap.put(STATUS, SITEID_OR_LIVE_NOEXIST);
				resultMap.put(MESSAGE, "siteId or live is not found in api");
				return JSONUtils.map2Json(resultMap);
			}

			logger.info("转账接口#######参数::::username:" + username + "#keyB#" + firstEntity.getKeyb() + "#时间#"+ StringsUtil.updateTime());
			if (!validKey(username, key, firstEntity)) {// md5验证
				logger.info("md5 is error");
				resultMap.put(STATUS, PARAM_FORMAT_ERROR);// 用户名
				resultMap.put(MESSAGE, "md5 is error");// 密码
				return JSONUtils.map2Json(resultMap);
			}
			logger.info("------------------->>进入 转账 接口<<-------------------");
			TransferParam param = new TransferParam(firstEntity, username, credit, billno, type, cur, null, live,playerIp,terminal,isDemo,null,false);
			TransferRecordDetailEntity record = new TransferRecordDetailEntity();
			record.setRemark(transMethod);
			record.setOperator(operator);
			record = this.transferRecordDetailService.insert(record, param);
			logger.info("转账记录插入成功, id = {}, billno = {}", record.getId(), record.getBillno());
			param.setTransRecordId(record.getId());
			if (!transMethod.contains("_")) {// 直接跟钱包中心交互 | 资金归集
				logger.info("直接跟钱包中心交互 | 资金归集...");
				if (TransferConstants.TransferMethod.BALANCE_TOTAL.equals(transMethod) && SysConstants.LiveId.TOTAL.equals(live)) {// 资金归集
					logger.info("资金归集...");
					if (!SysConstants.LiveId.TOTAL.equals(live)) {
						return JSONUtils.map2Json(failure("资金归集live = " + live + " is error!"));
					}
					List<ApiInfoEntity> queryApiInfoBySiteId = this.apiInfoService.queryApiInfoBySiteId(siteId);
					
					//闭锁线程数量
					CountDownLatch cdl = new CountDownLatch(queryApiInfoBySiteId.size());
					for (ApiInfoEntity apiInfoEntity : queryApiInfoBySiteId) {
						if("DS".equals(apiInfoEntity.getLiveName())){
							cdl.countDown();
							continue;
						}
						//封装资金归集实体
						BalanceTotalEntity bt = new BalanceTotalEntity();
						bt.setApiInfoEntity(apiInfoEntity);
						bt.setBillno(billno);
						bt.setCredit(credit);
						bt.setCur(cur);
						bt.setIsDemo(isDemo);
						bt.setId(record.getId());
						bt.setUsername(username);
						bt.setSiteIdLiveIdMap(siteIdLiveIdMap);
						bt.setSupportServiceMap(supportServiceMap);
						bt.setTransferServiceMap(transferServiceMap);
						bt.setCdl(cdl);
						bt.setResultMap(resultMap);
						bt.setIp(ip);
						
						//开启多线程资金收集到主账户
						taskExecutor.execute(new BalanceTotal(bt));
					}
					
					cdl.await();
					long totalCredit = 0;
					for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
						totalCredit += Long.parseLong((String)entry.getValue());
						record.setVersion(record.getVersion() + 1);
					}
					if (resultMap.size() == queryApiInfoBySiteId.size()) {
						resultMap = success("total success!");
					} else {
						resultMap = success("total failure!");
					}
					record.setCredit(String.valueOf(totalCredit));
				} else {// 单个平台转账
					logger.info("单个平台转账...");
					TransferService<?> transferService = transferServiceMap.get(transMethod);
					if (transferService == null) {
						resultMap.put(STATUS, ERROR);// 用户名
						resultMap.put(MESSAGE, "transMethod = " + transMethod + " is error");// 密码
						return JSONUtils.map2Json(resultMap);
					}
					if ("ds".equalsIgnoreCase(transMethod)) {
						// TODO: 以后增加产品 ,,进行修改
						param.setRemark("DS内部产品转" + (IN.equals(type) ? "入" : "出"));
					}
					resultMap = JSONUtils.json2Map(transferService.transfer(param));
				}
			} else {// 多个平台直接进行互转
				logger.info("多个平台直接进行互转...");
				String[] arr = transMethod.split("_");
				TransferService<?> firstTransfer = transferServiceMap.get(arr[0]);
				TransferService<?> secondTransfer = transferServiceMap.get(arr[1]);
				if (IN.equals(type)) {// 1.first --- 2.sencod +++
					param.setType(OUT);
					param.setRemark("(第一步)");
					resultMap = JSONUtils.json2Map(firstTransfer.transfer(param));
					if (SUCCESS.equals(resultMap.get(STATUS))) {
						record.setVersion(record.getVersion() + 1);
						param.setEntity(secordEntity);
						param.setType(type);
						param.setRemark("(第二步)");
						resultMap = JSONUtils.json2Map(secondTransfer.transfer(param));
					}
				} else { // 1.sencord --- 2.first +++
					param.setEntity(secordEntity);
					param.setType(type);
					param.setRemark("(第一步)");
					resultMap = JSONUtils.json2Map(secondTransfer.transfer(param));
					if (SUCCESS.equals(resultMap.get(STATUS))) {
						record.setVersion(record.getVersion() + 1);
						param.setEntity(firstEntity);
						param.setType(IN);
						param.setRemark("(第二步)");
						resultMap = JSONUtils.json2Map(firstTransfer.transfer(param));
					}
				}
			}
			
			// 更新状态
			if (SUCCESS.equals(resultMap.get(STATUS))) {
				this.transferRecordDetailService.update(record, TransferStatus.SUCCESS.value());
			} else if (ERROR.equals(resultMap.get(STATUS))) {
				this.transferRecordDetailService.update(record, TransferStatus.FAILURE.value());
			} else { // 其他状态码当作异常处理
				this.transferRecordDetailService.update(record, TransferStatus.MAYBE.value());
			}
		} catch (Exception e) {
			try {
				TransferRecordDetailEntity record = new TransferRecordDetailEntity();
				record.setRemark(transMethod);
				record.setOperator(operator);
				record.setBillno(billno);
				record.setUsername(username);
				TransferRecordDetailEntity query = this.transferRecordDetailService.query(record);
				this.transferRecordDetailService.updateDetailStatus(query, TransferStatus.SERVER_ERROR.value());
				/*TransferRecordEntity trEntity = new TransferRecordEntity();
				trEntity.setUsername(username);
				trEntity.setTransferMoney(credit);
				trEntity.setTransBillno(billno);
				trEntity.setTransStatus(TransferStatus.SERVER_ERROR.value());
				this.transferRecordService.update(TransferStatus.SERVER_ERROR.value(),"DS主账户转账程序异常,需人工检查!", trEntity);*/
			} catch (Exception e1) {
				logger.error("转账异常,更新转账信息异常!",e1);
			}
			logger.error("转账出错 : ", e);
			resultMap.put(STATUS, MAYBE);
			resultMap.put(MESSAGE, "system in error : " + e.getMessage());
		}
		logger.info("resultMap end = {}   ,   {}", resultMap, JSONUtils.map2Json(resultMap));
		return JSONUtils.map2Json(resultMap);
	}



	/**
	 * 
	 * @param username
	 * @param siteId
	 * @param isDemo
	 * @param live
	 * 存放AG=2 BBIN=11 DS=12 H8=13
	 * @return
	 */
	@RequestMapping(value = "queryBalance", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public @ResponseBody String queryBalance(String username, String siteId,String live,
			String cur,String key,String playerIp,String terminal,HttpServletRequest request) {
		logger.info("查询余额 : username={},siteId={},live={},cur={},key={},playerIp={}",username, siteId, live, cur, key,playerIp);
		String ip = StringsUtil.getIpAddr(request);// 获取请求的 ip
		String isDemo = PropsUtil.getProperty("isDemo");
		cur = StringsUtil.isNull(cur) ? "CNY" : cur;
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String result = null;
		try {
			resultMap.put("username", username);
			resultMap.put("live", live);
			resultMap.put("siteId", siteId);
			resultMap.put("key", key);

			resultMap = StringsUtil.isNull(resultMap);
			if (!SUCCESS.equals(resultMap.get(STATUS))) {
				return JSONUtils.map2Json(resultMap);
			}
			String siteIdKey = siteId + isDemo + live;
			Map<String, ApiInfoEntity> siteIdLiveIdMap = apiInfoService.getSiteIdLiveIdMap();
			ApiInfoEntity entity = siteIdLiveIdMap.get(siteIdKey);
			if (entity == null) {
				resultMap.put(STATUS, SITEID_OR_LIVE_NOEXIST);
				resultMap.put(MESSAGE, "siteId or live is not found in api");
				return JSONUtils.map2Json(resultMap);
			}
			if (!this.validKey(username, key, entity)) {
				resultMap.put(STATUS, PARAM_FORMAT_ERROR);
				resultMap.put(MESSAGE, "md5 is error");
				return JSONUtils.map2Json(resultMap);
			}
			QueryBalanceParam param = new QueryBalanceParam(entity, username, cur,ip,playerIp,terminal,isDemo);
			TransferService<?> transferService = transferServiceMap.get(entity.getLiveName().toLowerCase());
			if (transferService == null) {
				List<ApiInfoEntity> queryApiInfoBySiteId = this.apiInfoService.queryApiInfoBySiteId(siteId);
				double balance = 0d;
				StringBuffer tipBuffer = new StringBuffer();
				// 除ds 的都查询一遍
				for (ApiInfoEntity apiInfoEntity : queryApiInfoBySiteId) {
					if (SysConstants.LiveId.DS.equals(apiInfoEntity.getLiveId() + "")) {// DS主账户不用归集
						continue;
					}
					transferService = transferServiceMap.get(apiInfoEntity.getLiveName().toLowerCase());
					param.setEntity(apiInfoEntity);
					resultMap = JSONUtils.json2Map(transferService.queryBalance(param));
					if (SUCCESS.equals(resultMap.get(STATUS))) {
						balance += Double.valueOf(resultMap.get("balance") + "");
					} else {
						tipBuffer.append(apiInfoEntity.getLiveName()).append("没有查询成功,失败原因:").append(resultMap.get(MESSAGE));
					}
				}
				resultMap = tipBuffer.length() > 0 ? failure(tipBuffer.toString()) : success("success");
				resultMap.put("balance", balance);
				result = JSONUtils.map2Json(resultMap);
			} else { // 平台查询余额
				result = transferService.queryBalance(param);
			}
		} catch (Exception e) {
			logger.error("查询余额出错 : ", e);
			return JSONUtils.map2Json(maybe("query balance is exception"));
		}
		return result;
	}

	/**
	 * @param username
	 * @param siteId
	 * @param live
	 * @param lottoTray
	 * @param gameType
	 * @param cur
	 * @param lang
	 * @param isDemo
	 * @param key
	 * @param xiaoyuSiteId
	 * 经典彩的分公司id (仅经典彩对外使用)
	 * @return
	 */
	@RequestMapping(value = "login", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public @ResponseBody String login(String username, String siteId, String live, String lottoTray, String gameType, //
			String cur, String lang,String key, String xiaoyuSiteId,String lid,String platformURL,String app_id,
			String playerIp,String terminal,HttpServletRequest request, HttpServletResponse response) {
		String ip = StringsUtil.getIpAddr(request);
		String isDemo = PropsUtil.getProperty("isDemo");
		logger.info("login : username = {}, siteId = {}, live = {}, lottoTray = {}, gameType = {}, cur = {}, lang = {}, isDemo = {}, key = {}, ip = {},app_id={}", //
				username, siteId, live, lottoTray, gameType, cur, lang, isDemo, key, ip,app_id);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("username", username);
		resultMap.put("siteId", siteId);
		resultMap.put("live", live);
		resultMap.put("key", key);
		// 无意义参数r,过滤掉url后面注入的参数
		resultMap.put("r", System.currentTimeMillis());
		logger.info("无意义参数r---------" + resultMap.get("r") + "-------");
		resultMap = StringsUtil.isNull(resultMap);
		if (!SUCCESS.equals(resultMap.get(STATUS))) {
			return JSONUtils.map2Json(resultMap);
		}
		resultMap.clear();

		cur = StringsUtil.isNull(cur) ? SysConstants.CUR : cur;
		String siteIdKey = siteId + isDemo + live;
		logger.info("siteIdKey = {}", siteIdKey);
		Map<String, ApiInfoEntity> siteIdLiveIdMap = this.apiInfoService.getSiteIdLiveIdMap();
		ApiInfoEntity entity = siteIdLiveIdMap.get(siteIdKey);
		if (entity == null) {
			resultMap.put(STATUS, SITEID_OR_LIVE_NOEXIST);
			resultMap.put(MESSAGE, "siteId or live is not found in api");
			return JSONUtils.map2Json(resultMap);
		}
		if (!this.validKey(username, key, entity)) {
			resultMap.put(STATUS, PARAM_FORMAT_ERROR);
			resultMap.put(MESSAGE, "md5 is error");
			return JSONUtils.map2Json(resultMap);
		}
		String result = null;
		try {
			if (SysConstants.LiveId.AG.equals(live)) {
				gameType = StringsUtil.isNull(gameType) ? "0" : gameType;
				logger.info("ag login : gameType = {}", gameType);
				String logoutRedirectUrl = request.getParameter("logoutRedirectUrl");
				String loginAssignType = request.getParameter("loginAssignType");
				LoginParam loginParam = new LoginParam(entity, username,logoutRedirectUrl);
				loginParam.setGameType(gameType);
				loginParam.setLoginAssignType(loginAssignType);
				
				result = this.agTransferService.login(loginParam);
				logger.info("ag return result = {}", result);
				return result;
			} else if (SysConstants.LiveId.BBIN.equals(live)) {
				//[BB体育:ball][3D厅:3DHall][视讯:live][视讯:live][视讯:live][机率:game][若为空白则导入整合页]
				String pageSite = request.getParameter("pageSite");
				String gamekind = request.getParameter("gameKind");
				String gamecode = request.getParameter("gameCode");
				String isFlash = String.valueOf(request.getParameter("isFlash"));
				lang = StringsUtil.isNull(lang) ? SysConstants.LANGUAGE_Chinese : lang;
				//pageSite = StringsUtil.isNull(pageSite) ? "live" : pageSite;
				logger.info("bbin login : lang = {}, page_site = {}, gameType = {},isFlash={}, gamekind = {}, gamecode = {}", lang,pageSite, gameType,isFlash,gamekind, gamecode);
				if (StringsUtil.isNull(gameType)) {// 大厅登录
					LoginParam loginParam = new LoginParam(entity, username);
					loginParam.setLanguage(lang);
					loginParam.setPageSite(pageSite);
					loginParam.setLoginUrl(request.getParameter("loginUrl"));
					result = this.bbinTransferService.login(loginParam);
					logger.info("bbin 大厅 return result = {}", result);
					return result;
				} else {// 游戏登录
					if (!StringsUtil.isNull(gamekind) && !StringsUtil.isNumeric(gamekind)) {
						logger.info("bbin 电子游戏 return result = {}", result);
						return JSONUtils.map2Json(failure("gamekind is not Numberic"));
					}
					LoginParam loginParam = new LoginParam(entity, username);
					loginParam.setLanguage(lang);
					loginParam.setGamekind(gamekind);
					loginParam.setGameType(gameType);
					loginParam.setGamecode(gamecode);
					loginParam.setIsFlash(isFlash);
					loginParam.setLoginUrl(request.getParameter("loginUrl"));
					result = this.bbinTransferService.loginBySingGame(loginParam);// 登录
					logger.info("bbin 电子游戏 return result = {}", result);
					return result;
				}
			} /*else if (SysConstants.LiveId.DS.equals(live)) {// gameType:lotto|lottery
				String lottoType = request.getParameter("lottoType");// PC|PM
				String loginChannel = request.getParameter("loginChannel");
				String line = request.getParameter("line");// 线路(1, 2, …)
				line = StringsUtil.isNull(line) ? "1" : line;// 默认线路为1,
				lang = StringsUtil.isNull(lang) ? "CN" : lang;
				// LOTTERY时时彩|LOTTO香港彩|DS大厅登录|fenfen分分彩|xiaoyu经典彩
				if (StringsUtil.isNull(gameType)) {
					resultMap.put(STATUS, PARAM_FORMAT_ERROR);
					resultMap.put(MESSAGE, "gameType is null");
					return JSONUtils.map2Json(resultMap);
				}
				TransferService<?> transferService = transferServiceMap.get(gameType);
				if (transferService != null) {
					// fenfen lotto : line, accType, pageSite, username
					if ("fenfen".equals(gameType)) {
						resultMap.clear();
						if (StringsUtil.isNull(request.getParameter("action"))) { // fenfen-->商户密钥
							resultMap.put(STATUS, CUSTOMER_KEY_NULL);
							resultMap.put(MESSAGE, "customer key is null");
							return JSONUtils.map2Json(resultMap);
						}
						lottoType = StringsUtil.isNull(lottoType) ? "PC" : lottoType;
					}
					if ("xiaoyu".equals(gameType)) {
						resultMap.clear();
						if (StringsUtil.isNull(lottoTray)) {
							resultMap.put(STATUS, LOTTO_TRAY_NO_NULL);
							resultMap.put(MESSAGE, "lotto tray is null");
						}
					}
					logger.info("line = {}, action = {},loginChannel={}", line, request.getParameter("action"),loginChannel);
					LoginParam param = new LoginParam(entity, username);
					param.setAccType(request.getParameter("accType"));
					param.setAction(request.getParameter("action"));
					param.setBillno(request.getParameter("billno"));
					param.setCur(request.getParameter("cur"));
					param.setGamecode(request.getParameter("gameCode"));
					param.setGamekind(request.getParameter("gameKind"));
					param.setGameType(request.getParameter("gameType"));
					param.setLoginUrl(request.getParameter("loginUrl"));
					param.setLid(lid);
					param.setLoginAssignType(loginChannel);
					param.setPlatformURL(platformURL);
					param.setIsDemo(isDemo);
					param.setLanguage(lang);
					param.setLine(line);
					param.setLottoTray(lottoTray);
					param.setLottoType(lottoType);
					param.setOddType(request.getParameter("oddType"));
					param.setPageSite(request.getParameter("pageSite"));
					param.setUsername(username);
					result = transferService.login(param);
					logger.info("彩种 = {},login result={}", gameType, result);
					return result;
				}
				LoginParam param = new LoginParam(entity, username);
				param.setGameType(gameType);
				param.setCur(request.getParameter("cur"));
				param.setLanguage(lang);
				param.setLine(line);
				param.setLottoTray(lottoTray);
				param.setLottoType(lottoType);
				param.setLoginUrl(request.getParameter("loginUrl"));
				result = this.dsTransferService.login(param);
				logger.info("ds login return result = {}", result);
				return result;
			}else if(SysConstants.LiveId.LMG.equals(live)){
				username = request.getParameter("username");
				gameType = request.getParameter("gameType");
				lang = StringsUtil.isNull(lang) ? SysConstants.LANGUAGE : lang;
				String line = request.getParameter("line");
				LoginParam loginParam = new LoginParam(entity, username);
				loginParam.setLanguage(lang);
				loginParam.setCur(request.getParameter("cur"));
				loginParam.setGameType(gameType);
				loginParam.setLine(line);
				logger.info("LMG login : username = {}, gameType = {}, lang = {}, line = {}", username,gameType, lang,line);
				result = this.lmgTransferService.login(loginParam);
				logger.info("LMG 大厅 return username={},gameType={} result = {}",username,gameType,result);
				return result;
			} else if (SysConstants.LiveId.H8.equals(live)) { // h8 不能登陆
				String action = request.getParameter("action");
				String accType = request.getParameter("accType");
				String line = request.getParameter("line");
				String lottoType = request.getParameter("lottoType");
				action = StringsUtil.isNull(action) ? TransferConstants.H8_LOGIN_ACTION : action;
				accType = StringsUtil.isNull(accType) ? TransferConstants.H8_LOGIN_ACC_TYPE : accType;
				lang = StringsUtil.isNull(lang) ? SysConstants.LANGUAGE_Chinese : lang;
				logger.info("h8 login : action = {}, accType = {}, lang = {}, lottoType = {}, line = {}", action,accType, lang, lottoType, line);
				LoginParam loginParam = new LoginParam(entity, username);
				loginParam.setAction(action);
				loginParam.setAccType(accType);
				loginParam.setLottoType(lottoType);
				loginParam.setLanguage(lang);
				result = this.h8TransferService.login(loginParam);
				logger.info("h8 login return result = {}", result);
				return result;
			} else if (SysConstants.LiveId.OG.equals(live)) {// og登录 目前只有视讯
				//OG登录语言参数和MG相同,先用MG的
				lang = StringsUtil.isNull(lang) ? SysConstants.MG_LANGUAGE : lang;
				LoginParam param = new LoginParam(entity, username);
				String gameKind = request.getParameter("gameKind");
				param.setGameType(gameType);
				param.setGamekind(gameKind);
				param.setLanguage(lang);
				result = this.ogTransferService.login(param);
				logger.info("og login return result = {}", result);
				return result;
			} else if (SysConstants.LiveId.MG.equals(live)) {
				lang = StringsUtil.isNull(lang) ? SysConstants.MG_LANGUAGE : lang;
				String bankingUrl = request.getParameter("bankingUrl");
				String lobbyUrl = request.getParameter("lobbyUrl");
				String logoutRedirectUrl = request.getParameter("logoutRedirectUrl");
				// MG 登录
				logger.info("mg login username = {} ,appId={}, ip={}", username,app_id,ip);
				LoginParam loginParam = new LoginParam(entity, username);
				loginParam.setIsDemo(isDemo);
				loginParam.setGameType(gameType);
				loginParam.setBankingUrl(bankingUrl);
				loginParam.setLobbyUrl(lobbyUrl);
				loginParam.setLogoutRedirectUrl(logoutRedirectUrl);
				loginParam.setLanguage(lang);
				loginParam.setAppId(app_id);
				result = this.mgTransferService.login(loginParam);
				logger.info("mg return result={}", result);
				JSONObject json = JSONObject.parseObject(result);
				if (SUCCESS.equals(json.get(STATUS))) {
					String launchUrl = json.getString("launchUrl");
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("message", launchUrl);
					map.put("status", SUCCESS);
					return JSONUtils.map2Json(map);
				}
				return result;
			}else if (SysConstants.LiveId.XIAOYU.equals(live)) { // 经典彩登录,2017-06-27新增幸运 彩 对外
				String lottoType = request.getParameter("lottoType");
				String loginChannel = request.getParameter("loginChannel");
				logger.info("xiaoyu login type = {} loginChannel={}", lottoType, loginChannel);
				// LOTTERY时时彩|LOTTO香港彩|DS大厅登录|fenfen分分彩|xiaoyu经典彩
				if (StringsUtil.isNull(gameType)) {
					resultMap.put(STATUS, PARAM_FORMAT_ERROR);
					resultMap.put(MESSAGE, "gameType is null");
					return JSONUtils.map2Json(resultMap);
				}
				TransferService<?> transferService = transferServiceMap.get(gameType);
				if (transferService != null) {
					resultMap.clear();
					lottoTray = StringsUtil.isNull(lottoTray) ? "A" : lottoTray;
					if (StringsUtil.isNull(lottoTray)) {
						resultMap.put(STATUS, LOTTO_TRAY_NO_NULL);
						resultMap.put(MESSAGE, "lotto tray is null");
					}
					entity.setSiteId(Integer.valueOf(xiaoyuSiteId)); // 设置经典彩siteId
					LoginParam param = new LoginParam(entity, username);
					param.setAccType(request.getParameter("accType")); // 层级
					param.setOddType(request.getParameter("oddType"));
					param.setLottoTray(request.getParameter("lottoTray"));
					param.setLoginUrl(request.getParameter("loginUrl"));
					param.setTerminal("MP".equals(lottoType) ? lottoType : "PC");
					param.setUsername(username);
					param.setPlatformURL(platformURL);
					param.setLid(lid);
					param.setLoginAssignType(loginChannel);
					result = transferService.login(param);
					logger.info("彩种 = {}, login:{}return result : {}", gameType,loginChannel,result);
					return result;
				}
			}else if(SysConstants.LiveId.PT.equals(live)){
				String pageSite = request.getParameter("pageSite");
				String gamekind = request.getParameter("gameKind");
				String gamecode = request.getParameter("gameCode");
				lang = StringsUtil.isNull(lang) ? SysConstants.LANGUAGE_Chinese : lang;
				pageSite = StringsUtil.isNull(pageSite) ? "live" : pageSite;
				logger.info("PT login : lang = {}, page_site = {}, gameType = {}, gamekind = {}, gamecode = {}", lang,pageSite, gameType, gamekind, gamecode);
				LoginParam loginParam = new LoginParam(entity, username);
				loginParam.setLanguage(lang);
				loginParam.setPageSite(pageSite);
				result = this.ptTransferService.login(loginParam);
				logger.info("PT 大厅 return result = {}", result);
				return result;
			}else if(SysConstants.LiveId.KY.equals(live)){
				String gamekind = request.getParameter("gameKind");
				String gamecode = request.getParameter("gameCode");
				lang = StringsUtil.isNull(lang) ? SysConstants.LANGUAGE_Chinese : lang;
				logger.info("KY login : lang = {}, gameType = {}, gamekind = {}, gamecode = {}", lang, gameType, gamekind, gamecode);
				LoginParam loginParam = new LoginParam(entity, username);
				loginParam.setLanguage(lang);
				loginParam.setGamecode(gamecode);
				result = this.kyTransferService.login(loginParam);
				logger.info("KY 大厅 return result = {}", result);
				return result;
			}else if(SysConstants.LiveId.SGS.equals(live)){
				String gamecode = request.getParameter("gameCode");
				String isDemoParam=request.getParameter("isDemo");
				logger.info("sgs login username={},siteId={},gameType = {}, gamecode = {}",username,siteId,gameType, gamecode);
				LoginParam loginParam = new LoginParam(entity, username);
				loginParam.setGameType(gameType);
				loginParam.setGamecode(gamecode);
				loginParam.setTerminal(terminal);
				loginParam.setPlayerIp(playerIp);
				loginParam.setIsDemo("1".equals(isDemoParam)?"1":"0");//1试玩，0正式
				loginParam.setCur(cur);
				result = this.sgsTransferService.login(loginParam);
				logger.info("sgs login return loginParam = {}", loginParam.toString());
				logger.info("sgs login return result = {}", result);
				return result;
			}else if(SysConstants.LiveId.KKW.equals(live)){
				username = request.getParameter("username");
				gameType = request.getParameter("gameType");
				lang = StringsUtil.isNull(lang) ? SysConstants.LANGUAGE : lang;
				String line = request.getParameter("line");
				LoginParam loginParam = new LoginParam(entity, username);
				loginParam.setLanguage(lang);
				loginParam.setCur(request.getParameter("cur"));
				loginParam.setGameType(gameType);
				loginParam.setLine(line);
				logger.info("KKW login : username = {}, gameType = {}, lang = {}, line = {}", username,gameType, lang,line);
				result = this.kkwTransferService.login(loginParam);
				logger.info("KKW 大厅 return username={},gameType={} result = {}",username,gameType,result);
				return result;
			}*/else {
				resultMap.put(STATUS, PARAM_FORMAT_ERROR);
				resultMap.put(MESSAGE, "live isn't process way");
			}
		} catch (Exception e) {
			logger.error("login error : ", e);
			resultMap.put(STATUS, MAYBE);
			resultMap.put(MESSAGE, "system error");
		}
		return JSONUtils.map2Json(resultMap);
	}

	/**
	 * 查询订单状态
	 * @param username
	 * @param siteId
	 * @param live
	 * @param billno
	 * @param key
	 * @return
	 */
	@RequestMapping(value = "queryStatusByBillno", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String queryStatusByBillno(String username, String siteId, String billno, String key,
			HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String ip = StringsUtil.getIpAddr(request);
			logger.info("queryStatusByBillno : username = {}, siteId = {}, billno = {}, key = {}, ip={}",username, siteId, billno, key, ip);
			resultMap.put("username", username);
			resultMap.put("siteId", siteId);
			resultMap.put("billno", billno);
			resultMap.put("key", key);
			resultMap = StringsUtil.isNull(resultMap);
			if (!SUCCESS.equals(resultMap.get(STATUS))) {
				return JSONUtils.map2Json(resultMap);
			}
			resultMap.clear();

			TransferRecordDetailEntity record = new TransferRecordDetailEntity();
			record.setUsername(username);
			record.setSiteId(Integer.valueOf(siteId));
			record.setBillno(billno);
			record = this.transferRecordDetailService.query(record);
			if (record == null) {
				resultMap.put(STATUS, PARAM_FORMAT_ERROR);
				resultMap.put(MESSAGE, "订单号不存在!");
			}else if(record.getStatus() == SysConstants.Record.TRANS_FAILURE){
				resultMap.put(STATUS, ERROR);
				resultMap.put(MESSAGE,"转账失败!");
			}else if(record.getStatus() == SysConstants.Record.TRANS_MAYBE){
				resultMap.put(STATUS, MAYBE);
				resultMap.put(MESSAGE,"转账异常!");
			}else if(record.getStatus() == SysConstants.Record.TRANS_START){
				resultMap.put(STATUS, START_TRANSFER);
				resultMap.put(MESSAGE, "正在转账");
			}else if(record.getStatus() == SysConstants.Record.TRANS_SUCCESS){
				resultMap.put(STATUS, SUCCESS);
				resultMap.put(MESSAGE,"转账成功");
			}else{
				resultMap.put(STATUS,TRANS_UNKNOWN);
				resultMap.put(MESSAGE,"转账结果未知状态,请联系客服!");
			}
			logger.info("queryStatusByBillno username={},siteId={},billno={},result={}",username,siteId,billno,JSONUtils.map2Json(resultMap));
		} catch (Exception e) {
			logger.error("查询订单状态异常 : ", e);
			resultMap = failure("query exception");
		}
		return JSONUtils.map2Json(resultMap);
	}

	@RequestMapping(value = "queryRecord", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String queryRecord(String username, String siteId, String startTime, String endTime, String billno,
			String fromLive, String toLive, String type, String status, Integer page, Integer pageLimit
			, HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String ip = StringsUtil.getIpAddr(request);
			logger.info("queryRecord : username = {}, siteId = {}, billno = {}, fromLive = {}, toLive = {}, status = {}, startTime = {}, endTime = {}, page = {}, pageLimit = {}, ip={}", //
					username, siteId, billno, fromLive, toLive, status, startTime, endTime, page, pageLimit, ip);
			resultMap.put("siteId", siteId);
			resultMap = StringsUtil.isNull(resultMap);
			if (!SUCCESS.equals(resultMap.get(STATUS))) {
				return JSONUtils.map2Json(resultMap);
			}
			resultMap.clear();
			resultMap.put("username", username);
			resultMap.put("siteId", siteId);
			resultMap.put("billno", billno);
			resultMap.put("fromLive", fromLive);
			resultMap.put("toLive", toLive);
			resultMap.put("status", status);
			resultMap.put("startTime", startTime);
			resultMap.put("endTime", endTime);
			resultMap.put("page", page);
			resultMap.put("pageLimit", pageLimit);
			String result = this.transferRecordDetailService.queryRecordByPage(resultMap);
			logger.info("queryRecord return result count = {}, status = {}, message = {}", resultMap.get("total"),
					resultMap.get(STATUS), resultMap.get(MESSAGE));
			return result;
		} catch (Exception e) {
			logger.error("查询记录出错", e);
			return JSONUtils.map2Json(maybe("查询出错"));
		}
	}

	@RequestMapping(value = "queryRecordDetail", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String queryRecordDetail(Long transferRecordId, HttpServletRequest request) {
		try {
			String ip = StringsUtil.getIpAddr(request);
			logger.info("queryRecordDetail : transferRecordId = {}, ip = {}", transferRecordId, ip);
			if (transferRecordId == null) {
				return JSONUtils.map2Json(failure("transferRecordId is null"));
			}
			return this.transferRecordDetailService.queryRecordDetail(transferRecordId);
		} catch (Exception e) {
			logger.error("查询记录出错", e);
			return JSONUtils.map2Json(maybe("查询出错"));
		}
	}

	@RequestMapping(value = "totalBalanceBySiteId", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String totalBalanceBySiteId(String siteId, String live, String fromDate, String toDate) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		BigDecimal balance = new BigDecimal(0);
		try {
			logger.info("totalBalanceBySiteId : siteId = {}, live = {}, fromDate = {}, toDate = {}", siteId, live,fromDate, toDate);
			resultMap.put("live", live);
			resultMap.put("siteId", siteId);
			resultMap.put("fromDate", fromDate);
			resultMap.put("toDate", toDate);
			resultMap = StringsUtil.isNull(resultMap);
			if (!SUCCESS.equals(resultMap.get(STATUS))) {
				return JSONUtils.map2Json(resultMap);
			}
			resultMap.clear();
			String isDemo = PropsUtil.getProperty("isDemo");
			Map<String, ApiInfoEntity> siteIdLiveIdMap = apiInfoService.getSiteIdLiveIdMap();
			if (!live.contains("_")) { // 单平台
				return singLiveProccess(siteId, live, fromDate, toDate, resultMap, isDemo, siteIdLiveIdMap);
			} else { // 多平台
				return multipartLiveProcess(siteId, live, fromDate, toDate, balance, isDemo, siteIdLiveIdMap);
			}
		} catch (Exception e) {
			logger.error("统计余额出错 : ", e);
			return JSONUtils.map2Json(maybe("统计余额出错"));
		}
	}

	@RequestMapping(value = "changeH8OddType", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public String changeH8OddType(String siteId, String usernames, Integer maxCreditPerBet, Integer maxCreditPerMatch) {
		logger.info("changeH8OddType : siteId = {}, usernames = {}, maxCreditPerBet = {}, maxCreditPerMatch = {}",siteId, usernames, maxCreditPerBet, maxCreditPerMatch);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("siteId", siteId);
		resultMap.put("username", usernames);
		resultMap.put("maxCreditPerBet", maxCreditPerBet);
		resultMap.put("maxCreditPerMatch", maxCreditPerMatch);
		resultMap = StringsUtil.isNull(resultMap);
		if (!SUCCESS.equals(resultMap.get(STATUS))) {
			return JSONUtils.map2Json(resultMap);
		}
		if (maxCreditPerBet > maxCreditPerMatch) {
			return JSONUtils.map2Json(failure("maxCreditPerMatch should larger than maxCreditPerBet"));
		}
		if (StringsUtil.isNull(usernames)) {
			return JSONUtils.map2Json(failure("username is null"));
		}
		String isDemo = PropsUtil.getProperty("isDemo");
		String siteIdKey = siteId + isDemo + SysConstants.LiveId.H8;
		Map<String, ApiInfoEntity> siteIdMap = this.apiInfoService.getSiteIdLiveIdMap();
		ApiInfoEntity entity = siteIdMap.get(siteIdKey);
		if (entity == null) {
			return JSONUtils.map2Json(failure("siteId or live is not in api!"));
		}
		String result = null;
		StringBuffer buffer = new StringBuffer();
		if (usernames.contains(",")) {
			String[] usernameArr = usernames.split(",");
			for (String username : usernameArr) {
				//result = this.h8TransferService.changeOddType(entity, username, maxCreditPerBet, maxCreditPerMatch);
				resultMap = JSONUtils.json2Map(result);
				if (!SUCCESS.equals(resultMap.get(STATUS))) {
					buffer.append("username=").append(username).append("[reason=").append(resultMap.get(MESSAGE)).append("]").append(";");
				}
			}
		} else {
			//result = this.h8TransferService.changeOddType(entity, usernames, maxCreditPerBet, maxCreditPerMatch);
		}
		if (buffer.length() > 0) {
			buffer.append(" are not success , query the reason why failure!");
			return JSONUtils.map2Json(failure(buffer.toString()));
		}
		return result;
	}
	
	@RequestMapping(value = "checkTransfer", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public @ResponseBody String checkTransfer(String username, String siteId, String live, String billno){
		logger.info("查询转账确认接口 param= username={},siteId={},live={},billno={}",username,siteId,live,billno);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String isDemo = PropsUtil.getProperty("isDemo");
			String siteIdKey = siteId.concat(isDemo).concat(live);
			Map<String, ApiInfoEntity> siteIdLiveIdMap = apiInfoService.getSiteIdLiveIdMap();
			ApiInfoEntity entity = siteIdLiveIdMap.get(siteIdKey);
			QueryOrderStatusParam param = new QueryOrderStatusParam(entity,billno,username,null,null,null);
			Map<String, Object> checkResult = JSONUtils.json2Map(this.moneyCenterService.queryStatusByBillno(param));
			
			if(SUCCESS.equals(checkResult.get("status"))){
				resultMap.put(MESSAGE, "transfer success");
				resultMap.put(STATUS, SUCCESS);
			}else{
				resultMap.put(MESSAGE, "transfer failure");
				resultMap.put(STATUS, ERROR);
			}
		} catch (Exception e) {
			resultMap.put(STATUS, MAYBE);
			resultMap.put(MESSAGE, "check transfer error");
			logger.error("check transfer error!");
			e.printStackTrace();
		}
		logger.info("查询转账确认接口返回：{}",JSONUtils.map2Json(resultMap));
		return JSONUtils.map2Json(resultMap);
	}

	private String multipartLiveProcess(String siteId, String live, String fromDate, String toDate, BigDecimal balance,
			String isDemo, Map<String, ApiInfoEntity> siteIdLiveIdMap) throws InterruptedException, ExecutionException {
		Map<String, Object> resultMap = null;
		String[] lives = live.split("_");
		String result = null;
		String siteIdKey = null;
		ApiInfoEntity entity = null;
		SupportTransferService<?> supportService = null;
		// lives == 传过来的几家平台
		ExecutorService pool = Executors.newFixedThreadPool(lives.length);
		CountDownLatch latch = new CountDownLatch(lives.length);
		Map<String, Future<String>> resultFutureMap = new HashMap<String, Future<String>>();
		for (int i = 0; i < lives.length; i++) {
			siteIdKey = siteId + isDemo + lives[i];
			entity = siteIdLiveIdMap.get(siteIdKey);
			supportService = supportServiceMap.get(entity.getLiveName().toLowerCase());

			TotalBalanceByLive param = new TotalBalanceByLive(supportService, entity, fromDate, toDate, latch);
			Future<String> future = pool.submit(param);
			resultFutureMap.put(entity.getLiveName(), future);
		}
		latch.await();
		Set<Entry<String, Future<String>>> entrySet = resultFutureMap.entrySet();
		for (Entry<String, Future<String>> entry : entrySet) {
			result = entry.getValue().get();
			logger.info("totalBalanceBySiteId : live = {}, result = {}", entry.getKey(), result);
			resultMap = JSONUtils.json2Map(result);
			if (SUCCESS.equals(resultMap.get(STATUS))) {
				balance = balance.add(new BigDecimal(resultMap.get(MESSAGE) + ""));
			}
		}
		return JSONUtils.map2Json(success(balance.toPlainString()));
	}

	private String singLiveProccess(String siteId, String live, String fromDate, String toDate,
			Map<String, Object> resultMap, String isDemo, Map<String, ApiInfoEntity> siteIdLiveIdMap) {
		String siteIdKey = siteId + isDemo + live;
		ApiInfoEntity entity = siteIdLiveIdMap.get(siteIdKey);
		if (entity == null) {
			resultMap.put(STATUS, SITEID_OR_LIVE_NOEXIST);
			resultMap.put(MESSAGE, "siteId or live is not found in api");
			return JSONUtils.map2Json(resultMap);
		}
		TotalBalanceParam param = new TotalBalanceParam(entity, Integer.valueOf(siteId),
				entity.getLiveName().toLowerCase(), fromDate, toDate);
		SupportTransferService<?> supportTransferService = supportServiceMap.get(entity.getLiveName().toLowerCase());
		if (supportTransferService == null) {
			return JSONUtils.map2Json(failure("live = " + live + " is not exist !"));
		}
		return supportTransferService.totalBalanceBySiteId(param);
	}

	/**
	 * 通信验证key
	 */
	private boolean validKey(String username, String key, ApiInfoEntity entity) {
		StringBuilder keyParam = new StringBuilder();
		keyParam.append(username).append(entity.getKeyb()).append(StringsUtil.updateTime());
		key = key.substring(4, key.length() - 1);
		return key.equals(StringsUtil.toMD5(keyParam.toString()));
	}

	//限红验证
	private String limitAmountValidate(String siteId,String credit,Map<String, Object> resultMap) {
		credit = credit.contains(".") ? credit.substring(0,credit.indexOf(".")) : credit;
		//各网站限额
		if(null != TransferMoneyConstants.LIMIT_MONEY && TransferMoneyConstants.LIMIT_MONEY.size()>0){
			for (Iterator<LimitAmountInfo> iterator = TransferMoneyConstants.LIMIT_MONEY.iterator(); iterator.hasNext();) {
				LimitAmountInfo limitAmountInfo = (LimitAmountInfo) iterator.next();
				if(!StringsUtil.isNull(siteId) && siteId.equals(limitAmountInfo.getSiteId())){
					if(Integer.parseInt(credit) > Integer.parseInt(limitAmountInfo.getLimitAmount())){
						resultMap.put(STATUS, PARAM_FORMAT_ERROR);
						resultMap.put(MESSAGE, "It exceeds the maximum transfer limit");
						logger.info("{}超过最大限额{}", credit, TransferConstants.BIG_MONEY);
						return JSONUtils.map2Json(resultMap);
					}else{
						return null;
					}
				}
			}
		}

		if (Integer.valueOf(credit) > TransferConstants.BIG_MONEY) {
			resultMap.put(STATUS, PARAM_FORMAT_ERROR);
			resultMap.put(MESSAGE, "It exceeds the maximum transfer limit");
			logger.info("{}超过最大限额{}", credit, TransferConstants.BIG_MONEY);
			return JSONUtils.map2Json(resultMap);
		}
		return null;
	}
}

class BalanceTotal extends BaseController implements Runnable{
	private static Logger logger = LoggerFactory.getLogger(BalanceTotal.class);
	private BalanceTotalEntity balanceTotalEntity;
	protected static final String SUCCESS = "10000";
	
	public BalanceTotal(BalanceTotalEntity balanceTotalEntity) {
		this.balanceTotalEntity = balanceTotalEntity;
	}
	
	@Override
	public void run() {
		this.balanceTotal(balanceTotalEntity);
	}
	
	private void balanceTotal(BalanceTotalEntity balanceTotalEntity){
		Map<String, Object> resultMap = balanceTotalEntity.getResultMap();
		Map<String, TransferService<?>> transferServiceMap = balanceTotalEntity.getTransferServiceMap();
		ApiInfoEntity apiInfoEntity = balanceTotalEntity.getApiInfoEntity();
		String billno = balanceTotalEntity.getBillno();
		String credit = balanceTotalEntity.getCredit();
		String cur = balanceTotalEntity.getCur();
		String isDemo = balanceTotalEntity.getIsDemo();
		Long recordId = balanceTotalEntity.getId();
		String username = balanceTotalEntity.getUsername();
		String ip = balanceTotalEntity.getIp();
		String playerIp = balanceTotalEntity.getPlayerIp();
		String terminal = balanceTotalEntity.getTerminal();
		Integer siteId = apiInfoEntity.getSiteId();
		Integer liveId = apiInfoEntity.getLiveId();
		
		try {
			Map<String, ApiInfoEntity> siteIdLiveIdMap = balanceTotalEntity.getSiteIdLiveIdMap();
			QueryBalanceParam queryParam = balanceTotalEntity.getQueryParam();
			ApiInfoEntity entity = null;
			TransferService<?> transferService = null;
			entity = siteIdLiveIdMap.get(siteId + isDemo + liveId);
			queryParam = new QueryBalanceParam(entity, username, cur,ip,playerIp,terminal,isDemo);
			transferService = transferServiceMap.get(entity.getLiveName().toLowerCase());
			//资金归集查询余额
			Map<String, Object> balanceResult = JSONUtils.json2Map(transferService.queryBalance(queryParam));
			logger.info("balanceTotalQuery username={},site_id={},live_id={},result={}",username,siteId,liveId,balanceResult.toString());
			if(SUCCESS.equals(balanceResult.get(STATUS))) {
				credit = (String)balanceResult.get("balance");
				if (credit.contains(".")) {
					credit = credit.substring(0, credit.indexOf("."));// 取整数进行归集
				}
				if (Integer.valueOf(credit).intValue() <= 0) {
					logger.info("balanceTotal username={},siteId={},live_id={},credit={},平台余额为 0  静默 资金归集成功!",username,siteId,liveId, credit);
					resultMap.put(String.valueOf(liveId),"0");
					return ;
				}
				TransferParam transferParam = new TransferParam(entity, username, credit, billno, OUT, cur,"balanceTotal", liveId + "",playerIp,terminal,isDemo,recordId, true);
				Map<String, Object> transferResult = JSONUtils.json2Map(transferService.transfer(transferParam));
				logger.info("balanceTotalTreansfer username={},siteId={},liv_id={},result={}",username,siteId,liveId,transferResult);
				if (SUCCESS.equals(transferResult.get(STATUS))) {
					resultMap.put(String.valueOf(liveId),credit);
					logger.info("balanceTotalTreansfer username={},siteId={},live_id={},credit={},资金归集成功",username,siteId,liveId, credit);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("balanceTotalTreansfer username={},siteId={},live_id={},credit={},资金归集异常",username,siteId,liveId, credit);
		}finally{
			balanceTotalEntity.getCdl().countDown();
		}
	}
}
