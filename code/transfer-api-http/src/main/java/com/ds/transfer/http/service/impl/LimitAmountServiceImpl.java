package com.ds.transfer.http.service.impl;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.ds.transfer.http.constants.TransferMoneyConstants;
import com.ds.transfer.http.entity.LimitAmountInfo;
import com.ds.transfer.http.mapper.LimitAmountInfoMapper;

@Service("limitAmountServiceImpl")
public class LimitAmountServiceImpl{
	@Autowired
	private LimitAmountInfoMapper limitAmountInfoMapper;
	
	@PostConstruct
	@Scheduled(cron = "${spring.schedule}")
	public void querySiteLimitAmount() {
		List<LimitAmountInfo> limitAmountList = limitAmountInfoMapper.selectByExample();
		TransferMoneyConstants.LIMIT_MONEY.addAll(limitAmountList);
	}
}
