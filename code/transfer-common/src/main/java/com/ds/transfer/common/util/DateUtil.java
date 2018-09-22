package com.ds.transfer.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日期工具类
 * 
 * @author jackson
 *
 */
public class DateUtil {

	public static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	public static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);
	
	// 获取当前时间
	public static String getCurrentTime() {
		return sdf1.format(new Date(System.currentTimeMillis()));
	}
	
	public static String getUTCCurrentTime() {
		return String.valueOf(System.currentTimeMillis()+ 1000*60*60*12);
	}
	public static String getUTCCurrentTimeFormat() {
		return sdf2.format(new Date(System.currentTimeMillis()+1000*60*60*12));
	}
	
	public static String getCurrentTimeFormat() {
		return sdf2.format(new Date(System.currentTimeMillis()));
	}
	
	public static long dateDifference(String date1,String date2){
		long difference = 0;
		try {
			SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date begin=dfs.parse(date1);
			Date end = dfs.parse(date2);
			long between=(end.getTime()-begin.getTime())/1000;//除以1000是为了转换成秒
			difference = between/60;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return difference;
	}
	
	public static void main(String[] args) {
		long dateDifference = dateDifference("2018-04-03 00:08:00","2018-04-03 00:13:00");
		System.out.println(dateDifference);
		String currentTimeFormat = getCurrentTimeFormat();
		System.out.println(currentTimeFormat);
		
		System.out.println(getUTCCurrentTime());
	}
}
