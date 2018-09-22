package com.ds.transfer.sgs.utils;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils {
	private static  Logger logger = LoggerFactory.getLogger(DateUtils.class);
    public static SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * 得到UTC时间，类型为字符串，格式为"yyyy-MM-dd HH:mm"<br />
	 * 如果获取失败，返回null
	 * @return
	 */
	public static String getUTCTime() {
		
		StringBuffer UTCTimeBuffer = new StringBuffer();
		
		try {
			// 1、取得本地时间：
			Calendar cal = Calendar.getInstance();
			// 2、取得时间偏移量：
			int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
			// 3、取得夏令时差：
			int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
			// 4、从本地时间里扣除这些差量，即可以取得UTC时间：
			cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
			int year = cal.get(Calendar.YEAR);
			String month = format(String.valueOf(cal.get(Calendar.MONTH) + 1));
			String day = format(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
			String hour = format(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
			String minute = format(String.valueOf(cal.get(Calendar.MINUTE)));
			String second = format(String.valueOf(cal.get(Calendar.SECOND)));
			String mil = String.valueOf(cal.get(Calendar.MILLISECOND));

			UTCTimeBuffer.append(year).append("-").append(month).append("-")
					.append(day).append("T");
			UTCTimeBuffer.append(hour).append(":").append(minute).append(":")
			.append(second).append("Z");
			
		} catch (Exception e) {
			logger.error("获取UTC时间异常！",e);
			e.printStackTrace();
		}
		return UTCTimeBuffer.toString();
	}
	
	
	public static String getUTCTimeFormat() {
		StringBuffer UTCTimeBuffer = new StringBuffer();
		try {
			// 1、取得本地时间：
			Calendar cal = Calendar.getInstance();
			// 2、取得时间偏移量：
			int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
			// 3、取得夏令时差：
			int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
			// 4、从本地时间里扣除这些差量，即可以取得UTC时间：
			cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
			int year = cal.get(Calendar.YEAR);
			String month = format(String.valueOf(cal.get(Calendar.MONTH) + 1));
			String day = format(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
			String hour = format(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
			String minute = format(String.valueOf(cal.get(Calendar.MINUTE)));
			String second = format(String.valueOf(cal.get(Calendar.SECOND)));
			String mil = String.valueOf(cal.get(Calendar.MILLISECOND));
			UTCTimeBuffer.append(year).append("-").append(month).append("-")
					.append(day).append("T");
			UTCTimeBuffer.append(hour).append(":").append(minute).append(":")
			.append(second).append(".").append(mil).append("+00:00");
		} catch (Exception e) {
			logger.error("获取UTC时间异常！",e);
			e.printStackTrace();
		}
		return UTCTimeBuffer.toString();
	}
	
	//获取当前时间前几天的时间点
	public static String getFromNow(int day) {  
		Date date = new Date();  
		long UTCTime = (date.getTime()- 8*60*60*1000);
		long dateTime = (UTCTime / 1000) + day * 24 * 60 * 60;  
		date.setTime(dateTime * 1000);  
		return formatter2.format(date).replace(" ", "T").concat("+00:00");  
	}
	
	
	
	/**
	 * 将UTC时间转换为东八区时间
	 * @param UTCTime
	 * @return
	 */
	public static String getLocalTimeFromUTC(String UTCTime) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		java.util.Date UTCDate = null;
		String localTimeStr = null;
		try {
			UTCDate = format.parse(UTCTime);
			format.setTimeZone(TimeZone.getTimeZone("GMT-8"));
			localTimeStr = format.format(UTCDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return localTimeStr;
	}
	
	public static String format(String tempDate){
		int length = tempDate.length();
		if(length < 2){
			return "0" + tempDate;
		}
		return tempDate;
	}
	
	public static void main(String[] args) {
		String fromNow = getFromNow(-1);
		String utcTimeFormat = getUTCTimeFormat();
		System.out.println(utcTimeFormat);
		System.out.println(fromNow);
	}
}
