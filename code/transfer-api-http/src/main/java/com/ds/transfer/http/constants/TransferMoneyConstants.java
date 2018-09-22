package com.ds.transfer.http.constants;
import java.util.concurrent.ConcurrentLinkedDeque;
import com.ds.transfer.http.entity.LimitAmountInfo;

public class TransferMoneyConstants {
	/**
	 * 限红转账
	 */
	public static ConcurrentLinkedDeque<LimitAmountInfo> LIMIT_MONEY = new ConcurrentLinkedDeque<LimitAmountInfo>();
}
