package com.ds.transfer.scan.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);


	/**
	 * 向指定URL 发送 post方法请求
	 * @param url 发送请求的 URL
	 * @param param 请求参数，请求参数应该是 name1=value&name2=value2的形式
	 * @return URL所代表远程资源的响应
	 * @throws Exception 
	 */
	public static String sendPost1(String url, String param) throws Exception {
		logger.info("S : url = {}, param = {}", url, param);

		PrintWriter out = null;
		BufferedReader in = null;
		StringBuilder result = new StringBuilder();

		try {
			URL realUrl = new URL(url);
			URLConnection conn = realUrl.openConnection();
			conn.setConnectTimeout(60000);//连接超时时间
			conn.setReadTimeout(60000);
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("User-Agent", "WEB_LIB_GI_");
			//"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

			//发送POST请求必须设置如下两行 
			conn.setDoOutput(true);
			conn.setDoInput(true);

			//获取URLConnection对象对应的输出流 
			out = new PrintWriter(conn.getOutputStream());

			//发送请求参数
			out.print(param);

			//flush输出流的缓冲
			out.flush();

			//定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;

			while ((line = in.readLine()) != null) {
				result.append(line);
			}
		} catch (Exception e) {
			throw e;
		}
		logger.info("R : result = {}", result.toString());
		return result.toString();
	}

	public static String sendGet(String url, String param) throws Exception {
		logger.info("S : url = {}, param = {}", url, param);
		String result = "";
		BufferedReader in = null;
		try {
			String urlName = url + "?" + param;
			//System.out.println("urlName::"+urlName);
			URL realUrl = new URL(urlName);
			// 打开和URL之间的链接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			// 建立实际的链接
			conn.connect();
			// 获取所有响应头字段
			//Map<String, List<String>> map = conn.getHeaderFields();
			// 遍历所有的响应头字段
			//for (String key : map.keySet()) {
			//	System.out.println(key + "--->" + map.get(key));
			//}
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += "\n" + line;
				//System.out.println("result:"+result);
			}
		} catch (Exception e) {
			throw e;
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		logger.info("R : result = {}", result);
		return result;
	}


}