package com.ds.transfer.scan.job;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ds.transfer.common.service.CommonTransferService;
import com.ds.transfer.common.service.TransferService;
import com.ds.transfer.common.util.DateUtil;
import com.ds.transfer.common.util.EncryptUtils;
import com.ds.transfer.common.util.JSONUtils;
import com.ds.transfer.common.util.StringsUtil;
import com.ds.transfer.common.vo.QueryOrderStatusParam;
import com.ds.transfer.common.vo.TransferParam;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.ApiInfoEntityExample;
import com.ds.transfer.record.entity.TransferOtherRule;
import com.ds.transfer.record.entity.TransferOtherRuleExample;
import com.ds.transfer.record.entity.TransferRecordEntity;
import com.ds.transfer.record.mapper.ApiInfoEntityMapper;
import com.ds.transfer.record.mapper.TransferOtherRuleMapper;
import com.ds.transfer.record.mapper.TransferRecordEntityMapper;
import com.ds.transfer.record.service.TransferRecordService;
import com.ds.transfer.scan.constants.ApplicationConstants;
import com.ds.transfer.scan.constants.ScanConstants;
import com.ds.transfer.scan.entity.TransferAlarmEntity;
import com.ds.transfer.scan.entity.TransferAlarmEntityExample;
import com.ds.transfer.scan.entity.TransferMoneyKeyEntity;
import com.ds.transfer.scan.entity.TransferMoneyKeyEntityExample;
import com.ds.transfer.scan.entity.TransferRecordDetailEntity;
import com.ds.transfer.scan.entity.TransferRecordDetailEntityExample;
import com.ds.transfer.scan.entity.TransferRecordDetailEntityExample.Criteria;
import com.ds.transfer.scan.mapper.TransferAlarmEntityMapper;
import com.ds.transfer.scan.mapper.TransferMoneyKeyEntityMapper;
import com.ds.transfer.scan.mapper.TransferRecordDetailEntityMapper;
import com.ds.transfer.scan.service.TransferRecordDetailService;
import com.ds.transfer.scan.util.PropsUtil;

/**
 * 扫表定时任务
 * @author jackson
 */
@Component
public class ScanRecordService extends CommonTransferService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	//service 业务类
	@Resource(name = "agTransferService")
	private TransferService<?> agTransferService;

	@Resource(name = "bbinTransferService")
	private TransferService<?> bbinTransferService;

	@Resource(name = "dsTransferService")
	private TransferService<?> dsTransferService;

	/*@Resource(name = "h8TransferService")
	private TransferService<?> h8TransferService;

	@Resource(name = "ogTransferService")
	private TransferService<?> ogTransferService;
	
	@Resource(name = "ptTransferService")
	private TransferService<?> ptTransferService;
	
	@Resource(name = "lmgTransferService")
	private TransferService<?> lmgTransferService;
	
	@Resource(name = "kyTransferService")
	private TransferService<?> kyTransferService;
	
	@Resource(name = "sgsTransferService")
	private TransferService<?> sgsTransferService;
	
	@Resource(name = "kkwTransferService")
	private TransferService<?> kkwTransferService;*/

	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;

	@Resource
	private TransferAlarmEntityMapper transferAlarmEntityMapper;

	@Resource(name = "transferRecordDetailServiceImpl")
	private TransferRecordDetailService transferRecordDetailService;

	@Resource
	private TransferRecordDetailEntityMapper transferRecordDetailEntityMapper;

	@Resource
	private TransferRecordEntityMapper transferRecordEntityMapper;

	@Resource
	private TransferMoneyKeyEntityMapper transferMoneyKeyEntityMapper;

	@Resource
	private ApiInfoEntityMapper apiInfoEntityMapper;
	
	@Resource
	private TransferOtherRuleMapper transferOtherRuleMapper;

	//存放moneyKey
	private List<TransferMoneyKeyEntity> moneyKeyList = null;

	//存放moneyKey
	private Map<String, Integer> moneyKeyMap = new ConcurrentHashMap<String, Integer>();//key=live + type,value=fromKeyType

	//存放api_info
	private Map<String, ApiInfoEntity> apiInfoMap = new ConcurrentHashMap<String, ApiInfoEntity>();//key=siteId + liveId

	//存放转账业务类
	private Map<String, TransferService<?>> transferServiceMap = ApplicationConstants.transerServiceMap;

	private CountDownLatch latch = null;

	@PostConstruct
	public void initTransMoneyKeyList() {
		initData();
	}

	private void initData() {
		//1.加载 fromKeyType
		TransferMoneyKeyEntityExample example = new TransferMoneyKeyEntityExample();
		moneyKeyList = this.transferMoneyKeyEntityMapper.selectByExample(example);
		for (TransferMoneyKeyEntity entity : moneyKeyList) {
			moneyKeyMap.put(entity.getLiveId() + entity.getType(), entity.getFromKeyType());
		}
		//2.加载api_info
		ApiInfoEntityExample apiInfoExample = new ApiInfoEntityExample();
		com.ds.transfer.record.entity.ApiInfoEntityExample.Criteria apiInfoCriteria = apiInfoExample.createCriteria();
		apiInfoCriteria.andIsDemoEqualTo(Integer.valueOf(PropsUtil.getProperty("isDemo")));
		List<ApiInfoEntity> apiInfoList = this.apiInfoEntityMapper.selectByExample(apiInfoExample);
		for (ApiInfoEntity entity : apiInfoList) {
			logger.info("初始化apiinfoentity = {}", entity.getSiteId() + "" + entity.getLiveId());
			apiInfoMap.put(entity.getSiteId() + "" + entity.getLiveId(), entity);
		}
	}

	@Scheduled(cron = "0 0 0/1 * * ?")
	public void updateTransMoneyKeyList() {
		initData();
	}

	/**
	 * 扫描不成功的记录进行处理
	 */
	@Scheduled(cron = "0 0/1 * * * ?")
	public void scanUnSuccessRecord() {

		logger.info("开启定时任务 : scanUnSuccessRecord");
		try {
			if (latch != null) {
				latch.await();
				logger.info("执行完一次拉取...");
			}
			//查询转账异常数据，AG,OG延迟三分钟再执行,MG,PMG不添加扫描,
			List<TransferRecordEntity> maybeRecordList = this.transferRecordEntityMapper.scanTransferError();
			logger.info("查询出问题数据数量 = {}", maybeRecordList.size());
			if (maybeRecordList != null && maybeRecordList.size() > 0) {
				latch = new CountDownLatch(maybeRecordList.size());
				for (TransferRecordEntity entity : maybeRecordList) {
					new Thread(new MaybeProcessThread(entity, transferRecordEntityMapper, 
							transferRecordService, transferRecordDetailEntityMapper, transferRecordDetailService,
							transferAlarmEntityMapper,transferOtherRuleMapper,latch)).start();
				}
			}
		} catch (Exception e) {
			logger.error("任务出错 : ", e);
			e.printStackTrace();
		}
	}

	public class MaybeProcessThread implements Runnable {

		//记录详情
		private TransferRecordEntity entity;
		private TransferRecordService transferRecordService;
		private TransferRecordEntityMapper transferRecordEntityMapper;

		//单条记录
		private TransferRecordDetailEntityMapper transferRecordDetailEntityMapper;
		private TransferRecordDetailService transferRecordDetailService;
		private TransferOtherRuleMapper transferOtherRuleMapper;
		//报警
		private TransferAlarmEntityMapper transferAlarmEntityMapper;

		private CountDownLatch latch;

		public MaybeProcessThread(TransferRecordEntity entity, TransferRecordEntityMapper transferRecordEntityMapper, TransferRecordService transferRecordService, //
				TransferRecordDetailEntityMapper transferRecordDetailEntityMapper, TransferRecordDetailService transferRecordDetailService, //
				TransferAlarmEntityMapper transferAlarmEntityMapper, TransferOtherRuleMapper transferOtherRuleMapper,//
				CountDownLatch latch) {
			this.entity = entity;
			this.transferRecordService = transferRecordService;
			this.transferRecordDetailEntityMapper = transferRecordDetailEntityMapper;
			this.transferRecordEntityMapper = transferRecordEntityMapper;
			this.transferRecordDetailService = transferRecordDetailService;
			this.transferAlarmEntityMapper = transferAlarmEntityMapper;
			this.transferOtherRuleMapper = transferOtherRuleMapper;
			this.latch = latch;
		}

		@Override
		public void run() {
			TransferAlarmEntity alarm = null;
			ApiInfoEntity apiInfoEntity = null;
			TransferRecordDetailEntityExample detailExample = null;
			List<TransferRecordDetailEntity> detailList = null;
			TransferAlarmEntityExample alarmExample = null;
			List<TransferAlarmEntity> alarmList = null;
			TransferRecordDetailEntity detail = null;
			Map<String, Object> resultMap = null;
			logger.info("开始处理数据记录：{}",entity.toString());
			try {
				//判断条目的合法性，如发现人工修改，则直接抛出异常
				if (!validateFinger(entity)) {
					logger.info("转账记录id:" + entity.getId() + "被手工修改.....");
					throw new Exception("发现转账记录有手工修改");
				}
				detailExample = new TransferRecordDetailEntityExample();
				Criteria detailCriteria = detailExample.createCriteria();
				detailCriteria.andIdEqualTo(entity.getTransRecordId());
				detailList = this.transferRecordDetailEntityMapper.selectByExample(detailExample);
				if (detailList != null && detailList.size() > 0) {
					detail = detailList.get(0);//单条转帐记录
					apiInfoEntity = apiInfoMap.get(entity.getSiteId() + "" + entity.getLiveId());
					if (apiInfoEntity == null) {
						logger.error("");
						return;
					}
					if (ScanConstants.TransType.IN.equals(entity.getTransType())) { //转入		
						if (ScanConstants.LiveType.DS.equals(entity.getLiveType())) { //DS
							resultMap = queryDsBillnoStatus(detail, entity);
							if (SUCCESS.equals(resultMap.get(STATUS))) { // 正常返回
								logger.info("转入DS主账户异常 -------------->> 状态确认成功...");
								this.transferRecordService.update(ScanConstants.TransStatus.SUCCESS, entity.getRemark(), entity);
								this.transferRecordDetailService.update(detail, ScanConstants.TransStatus.SUCCESS, detail.getRemark() + ",状态确认为成功!");
							} else if (ERROR.equals(resultMap.get(STATUS))) {//失败,进入报警表
								insertAlarm(resultMap, entity, "转入DS主账户异常或失败:" + resultMap.get(MESSAGE), alarmExample, alarmList, alarm);
								this.transferRecordService.update(ScanConstants.TransStatus.FAILURE, entity.getRemark() + ",需客服手动处理!", entity);
								this.updateRecord(detail, ScanConstants.TransStatus.SERVICE_PROCESS, entity.getRemark() + ",需客服手动处理!");
							} else {//异常
								insertAlarm(resultMap, entity, "转入DS主账户异常或失败:" + resultMap.get(MESSAGE), alarmExample, alarmList, alarm);
							}
						} else { //!DS
							resultMap = queryNotDsTranStatus(apiInfoEntity, entity,transferOtherRuleMapper);
							logger.info("transId={},id={}查询转账状态返回：{}",entity.getTransBillno(),entity.getId(),resultMap.toString());
							if (SUCCESS.equals(resultMap.get(STATUS))) {
								logger.info("transId={},id={},转入{}异常 -------------->> 状态确认成功...", entity.getTransBillno(),entity.getId(),entity.getLiveType());
								//更新详情转账记录
								this.transferRecordService.update(ScanConstants.TransStatus.SUCCESS, entity.getRemark() + ",状态确认成功!", entity);
								this.updateRecord(detail, ScanConstants.TransStatus.SUCCESS, detail.getRemark() + ",状态确认成功!");
							} else if (ERROR.equals(resultMap.get(STATUS))) {
								logger.info("transId={},id={},转入{}异常 -------------->> 状态确认失败...",entity.getTransBillno(),entity.getId(),entity.getLiveType());
								//转入DS主账户
								resultMap = transferInDs(apiInfoEntity, entity);
								logger.info("transId={},id={}转账确认异常,转入主账户调用转账接口返回：{}",entity.getTransBillno(),entity.getId(),resultMap.toString());
								if (SUCCESS.equals(resultMap.get(STATUS))) {
									this.transferRecordService.update(ScanConstants.TransStatus.FAILURE, entity.getRemark() + ",已重新转入DS主账户!", entity);
									logger.info("补单转入DS主账户成功, billno = {}", entity.getTransBillno());
									this.updateRecord(detail, ScanConstants.TransStatus.SUCCESS, ",已重新转入DS主账户");
								} else {//失败或异常,,进入报警表
									logger.info("billno = {}, DS转入失败", entity.getTransBillno());
									this.transferRecordService.update(ScanConstants.TransStatus.FAILURE, entity.getRemark() + ",重新转入DS主账户失败,需客服手动处理!", entity);
									this.updateRecord(detail, ScanConstants.TransStatus.FAILURE, detail.getRemark() + ",重新转入DS主账户失败,需客服手动处理!");
									insertAlarm(resultMap, entity, "转入DS主账户异常或失败:" + resultMap.get(MESSAGE), alarmExample, alarmList, alarm);
								}
							} else {
								logger.info("转入{}异常 -------------->> 状态确认异常...等待下次任务", entity.getLiveType());
							}
						}
					} else { //转出
						if (ScanConstants.LiveType.DS.equals(entity.getLiveType())) { //DS
							resultMap = queryDsBillnoStatus(detail, entity);
							if (SUCCESS.equals(resultMap.get(STATUS))) { // 正常返回
								this.transferRecordService.update(ScanConstants.TransStatus.SUCCESS, entity.getRemark() + ",准备重新转入DS主账户!", entity);
								logger.info("transId={},id={}转出DS主账户异常 -------------->> 确认成功,准备重新转入DS主账户......",entity.getTransBillno(),entity.getId());
								resultMap = transferInDs(apiInfoEntity, entity);
								logger.info("transId={},id={}转出DS主账户异常 ,准备重新转入DS主账户",entity.getTransBillno(),entity.getId());
								if (SUCCESS.equals(resultMap.get(STATUS))) {//补单成功
									//插入转入DS主账户的记录
									logger.info("补单转入DS主账户成功, billno = {}", entity.getTransBillno());
									this.updateRecord(detail, ScanConstants.TransStatus.SUCCESS, detail.getRemark() + ",已重新转入DS主账户!");
								} else {//失败或异常,,进入报警表
									logger.info("billno = {}, DS转出成功,DS转入失败", entity.getTransBillno());
									this.updateRecord(detail, ScanConstants.TransStatus.FAILURE, detail.getRemark() + ",重新转入DS主账户失败,需客服手动处理!");
									insertAlarm(resultMap, entity, "转入DS主账户异常或失败:" + resultMap.get(MESSAGE), alarmExample, alarmList, alarm);
								}
							} else if (ERROR.equals(resultMap.get(STATUS))) { //转出DS失败
								logger.info("billno = {} ,DS转出失败无需处理", entity.getTransBillno());
								this.transferRecordService.update(ScanConstants.TransStatus.FAILURE, entity.getRemark() + ",DS转出失败无需处理!", entity);
								this.updateRecord(detail, ScanConstants.TransStatus.FAILURE, detail.getRemark() + ",DS转出失败无需处理!");
								insertAlarm(resultMap, entity, "转出DS主账户失败(无需处理):" + resultMap.get(MESSAGE), alarmExample, alarmList, alarm);
							} else {
								logger.info("转出DS主账户异常 -------------->> 状态确认异常...等待下次任务");
							}
						} else { // !DS
							resultMap = queryNotDsTranStatus(apiInfoEntity, entity,transferOtherRuleMapper);
							if (SUCCESS.equals(resultMap.get(STATUS))) { //转出成功
								this.transferRecordService.update(ScanConstants.TransStatus.SUCCESS, entity.getRemark() + ",准备重新转入DS主账户!", entity);
								logger.info("transId={},id={},转出{}异常 -------------->> 确认成功,准备重新转入DS主账户......", entity.getTransBillno(),entity.getId(),entity.getLiveType());
								resultMap = transferInDs(apiInfoEntity, entity);
								if (SUCCESS.equals(resultMap.get(STATUS))) {
									logger.info("补单转入DS主账户成功, billno = {}", entity.getTransBillno());
									this.updateRecord(detail, ScanConstants.TransStatus.SUCCESS, detail.getRemark() + ",已重新转入DS主账户!");
								} else {
									logger.info("billno = {}, {}转出成功,DS转入失败", entity.getTransBillno(), entity.getLiveType());
									this.updateRecord(detail, ScanConstants.TransStatus.FAILURE, detail.getRemark() + ",重新转入DS主账户失败,需客服手动处理!");
									insertAlarm(resultMap, entity, "转入DS主账户失败或异常 : " + resultMap.get(MESSAGE), alarmExample, alarmList, alarm);
								}
							} else if (ERROR.equals(resultMap.get(STATUS))) { //转出失败
								logger.info("转出{},transId={}失败,无需处理!", entity.getLiveType(), entity.getTransBillno());
								this.transferRecordService.update(ScanConstants.TransStatus.FAILURE, entity.getRemark() + "," + entity.getLiveType() + "转出失败,请客服核查!", entity);
								this.updateRecord(detail, ScanConstants.TransStatus.FAILURE, detail.getRemark() + "," + entity.getLiveType() + "转出失败,请客服核查!");
							} else { //转出异常
								logger.info("转出{}异常 -------------->> 状态确认异常...等待下次任务", entity.getLiveType());
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("线程任务出错 : ", e);
			} finally {
				latch.countDown();
			}
		}

		/**
		 * 查询ds订单状态
		 * @param detail
		 * @param alarmExample
		 * @param alarmList
		 * @param alarm
		 * @param entity
		 */
		private Map<String, Object> queryDsBillnoStatus(TransferRecordDetailEntity detail, TransferRecordEntity entity) {
			ApiInfoEntity apiInfoEntity = apiInfoMap.get(entity.getSiteId() + "" + entity.getLiveId());
			if (apiInfoEntity == null) {
				throw new RuntimeException("不存在apiinfo前缀 = " + entity.getSiteId() + "" + ScanConstants.liveId.DS);
			}
			QueryOrderStatusParam queryBillnoStatusParam = new QueryOrderStatusParam(apiInfoEntity, entity.getTransBillno() + entity.getTransType(), entity.getUsername(), entity.getPassword(), entity.getTransType(), entity.getTransferMoney());
			logger.info("entity = {}, transBillno = {}, transType = {}, username = {}, password = {}, transMoney = {}",//
					apiInfoEntity, entity.getTransBillno(), entity.getTransType(), entity.getUsername(), entity.getPassword(), entity.getTransferMoney());
			String result = dsTransferService.queryStatusByBillno(queryBillnoStatusParam);
			logger.info("ds query billno status = {}", result);
			if (StringsUtil.isNull(result)) {
				throw new RuntimeException("ds内部转账 返回数据为空");
			}
			return JSONUtils.json2Map(result);

		}

		/**
		 * 更新单次转账记录表
		 * @param detail
		 */
		private void updateRecord(TransferRecordDetailEntity detail, Integer status, String remark) {
			detail.setStatus(status);
			detail.setUpdateTime(DateUtil.getCurrentTime());
			detail.setRemark(remark);
			this.transferRecordDetailEntityMapper.updateByPrimaryKey(detail);
		}

		/**
		 * 添加报警记录
		 * @param resultMap
		 * @param entity
		 */
		private void insertAlarm(Map<String, Object> resultMap, TransferRecordEntity entity, String remark, TransferAlarmEntityExample alarmExample, List<TransferAlarmEntity> alarmList, TransferAlarmEntity alarm) {
			alarmExample = new TransferAlarmEntityExample();
			com.ds.transfer.scan.entity.TransferAlarmEntityExample.Criteria alarmCriteria = alarmExample.createCriteria();
			alarmCriteria.andBillnoEqualTo(entity.getTransBillno());
			alarmList = this.transferAlarmEntityMapper.selectByExample(alarmExample);
			if (alarmList != null && alarmList.size() > 0) {
				alarm = alarmList.get(0);
				alarm.setStatus(ERROR.equals(resultMap.get(STATUS)) ? ScanConstants.TransStatus.FAILURE//
						: ScanConstants.TransStatus.MAYBE);
				alarm.setUpdateTime(new Date());
				alarm.setVersion(alarm.getVersion() + 1);
				this.transferAlarmEntityMapper.updateByPrimaryKey(alarm);
			} else {
				alarm = new TransferAlarmEntity();
				alarm.setBillno(entity.getTransBillno());
				alarm.setStatus(ERROR.equals(resultMap.get(STATUS)) ? ScanConstants.TransStatus.FAILURE//
						: ScanConstants.TransStatus.MAYBE);
				alarm.setRemark(remark);
				alarm.setCreateTime(new Date());
				alarm.setVersion(0);
				this.transferAlarmEntityMapper.insert(alarm);
			}
		}

		/**
		 * 查询非ds转账状态 
		 */
		private Map<String, Object> queryNotDsTranStatus(ApiInfoEntity apiInfoEntity, TransferRecordEntity entity,TransferOtherRuleMapper transferOtherRuleMapper) {
			String prefix = ScanConstants.LiveType.DS.equalsIgnoreCase(entity.getLiveType()) ? "ds" : entity.getLiveType().toLowerCase();
			logger.info("!DS 转账前缀 = {},transId = {},id = {}", prefix,entity.getTransBillno(),entity.getId());
			TransferService<?> transferService = transferServiceMap.get(prefix);
			String billno = entity.getTransBillno();
			
			if(ScanConstants.LiveType.KY.equals(prefix)){
				TransferOtherRuleExample transferOther = new TransferOtherRuleExample();
				com.ds.transfer.record.entity.TransferOtherRuleExample.Criteria createCriteria = transferOther.createCriteria();
				createCriteria.andTransCenterBillnoEqualTo(entity.getTransBillno());
				createCriteria.andSiteIdEqualTo(entity.getSiteId());
				createCriteria.andLiveIdEqualTo(entity.getLiveId());
				List<TransferOtherRule> transferResult = this.transferOtherRuleMapper.selectByExample(transferOther);
				if(transferResult != null && transferResult.size() > 0){
					billno = transferResult.get(0).getTransMappingBillno();
				}else{
					logger.info("KY 转账前缀 = {},transId = {},id = {} 开元棋牌查询映射转账号为空！！！", prefix,entity.getTransBillno(),entity.getId());
				}
				logger.info("KY 转账前缀 = {},transId = {},transMappingBillno={},id = {} 开元棋牌映射转账号！",prefix,entity.getTransBillno(),billno,entity.getId());
			}
			QueryOrderStatusParam param = new QueryOrderStatusParam(apiInfoEntity, billno, //
					entity.getUsername(), entity.getPassword(), entity.getTransType(), entity.getTransferMoney());
			String result = transferService.queryStatusByBillno(param);
			logger.info("!DS 转账状态确认transId={},id={},result = {}",billno,entity.getId(),result);
			Map<String, Object> resultMap = null;
			try {
				resultMap = JSONUtils.json2Map(result);
			} catch (Exception e) {
				logger.info("JOSN转换异常!",e);
			}
			return resultMap;
		}

		/**
		 * 钱转入ds
		 */
		private Map<String, Object> transferInDs(ApiInfoEntity apiInfoEntity, TransferRecordEntity entity) {
			// 转入DS主账户
			try {
				logger.info("ds 转账 用户名 username = {}, prefix = {},transId={},id={}", entity.getUsername(), apiInfoEntity.getPrefix(),entity.getTransBillno(),entity.getId());
				TransferParam transferParam = new TransferParam(apiInfoEntity, entity.getUsername(), //
						entity.getTransferMoney(), entity.getTransBillno() + "R", IN, apiInfoEntity.getCurrencyType(),
						"转入" + entity.getLiveType() + "失败,重新转入DS主账户", entity.getLiveId() + "",null,null,null,entity.getTransRecordId(), false);
				String result = dsTransferService.transfer(transferParam);
				logger.info("ds 转账 用户名 username = {}, prefix = {},transId={},id={} 转入DS result：{}", entity.getUsername(), apiInfoEntity.getPrefix(),entity.getTransBillno(),entity.getId(),result);
				return JSONUtils.json2Map(result);
			} catch (Exception e) {
				logger.error("DS主账户转账异常 : ", e);
				return maybe("DS主账户转账异常!");
			}
		}

		/**
		 * 补单添加记录
		 */
		public int countVersion(Integer version) {
			if (version == null) {
				version = 1;
			} else {
				version++;
			}
			return version;
		}
	}

	private boolean validateFinger(TransferRecordEntity entity) {
		String finger = entity.getFinger();
		String enFinger = EncryptUtils.encrypt(entity.getUsername(), entity.getTransferMoney(), entity.getTransBillno(), entity.getTransStatus().toString());
		if (finger.equals(enFinger)) {
			return true;
		}
		return false;
	}
}
