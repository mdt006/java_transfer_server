package com.ds.transfer.http.service.impl;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ds.transfer.common.service.CommonTransferService;
import com.ds.transfer.common.util.DateUtil;
import com.ds.transfer.http.service.TransRecordService;
import com.ds.transfer.record.entity.TransferRecordEntity;
import com.ds.transfer.record.mapper.TransferRecordEntityMapper;

@Service("transRecordServiceImpl")
public class TransRecordServiceImpl  extends CommonTransferService implements TransRecordService {
	@Resource
	private TransferRecordEntityMapper transferRecordEntityMapper;
	
	/**
	 * 更新转账记录
	 */
	@Transactional
	@Override
	public TransferRecordEntity update(Integer transferStatus, String remark, TransferRecordEntity record) {
		record.setTransStatus(transferStatus);
		record.setUpdateTime(DateUtil.getCurrentTime());
		record.setRemark(remark);
		this.transferRecordEntityMapper.updateByPrimaryKey(record);
		return record;
	}
}
