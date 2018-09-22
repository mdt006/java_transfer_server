package com.ds.transfer.http.service;
import com.ds.transfer.record.entity.TransferRecordEntity;

public interface TransRecordService {
	
	TransferRecordEntity update(Integer transferStatus, String remark, TransferRecordEntity record);

}
