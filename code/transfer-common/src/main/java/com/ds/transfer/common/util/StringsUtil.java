package com.ds.transfer.common.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 工具类
 */
public class StringsUtil {

	private static Logger logger = LoggerFactory.getLogger(StringsUtil.class);

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
	private static long startVaue = 0;
	static Random random = new Random();

	static char[] str = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	public static String randomString(int digit) {

		StringBuilder build = new StringBuilder(digit);
		for (int i = 0; i < digit; i++) {
			build.append(str[random.nextInt(26)]);
		}

		return build.toString();
	}
	
	
	public static synchronized String getRandomNum() {
		java.text.SimpleDateFormat oFormat;
		startVaue++;
		startVaue = startVaue % 1000;
		java.text.DecimalFormat format = new java.text.DecimalFormat("000");
		String sStartVaue = format.format(startVaue);
		Date oToday = new Date();
		oFormat = new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS");
		String sDate = oFormat.format(oToday);
		String id = sDate + sStartVaue;
		return id;
	}
	
	public static void main(String[] args) {
		
		String randomString = randomString(12);
		System.out.println(randomString);
		String randomNum = getRandomNum();
		System.out.println(randomNum);
	}
	

	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null) {
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_CLIENT_IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}
		}
		return ip;
	}

	//获取当天美东时间
	public static String updateTime() {
		return sdf.format(new Date(new Date().getTime()));//-43200000   -43500000
	}

	public static String toMD5(String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			byte[] byteDigest = md.digest();

			StringBuilder buf = new StringBuilder("");
			for (int offset = 0; offset < byteDigest.length; offset++) {
				int i = byteDigest[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			return buf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
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
			conn.setConnectTimeout(50000);//连接超时时间
			conn.setReadTimeout(50000);
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
			logger.error("post error : ", e);
			throw e;
		}
		logger.info("R : result = {}", result.toString());
		return result.toString();
	}

	public static String sendGet(String url) throws Exception {
		logger.info("S : url = {}", url);
		String result = "";
		BufferedReader in = null;
		try {
			String urlName = url;
			//System.out.println("urlName::"+urlName);
			URL realUrl = new URL(urlName);
			// 打开和URL之间的链接
			URLConnection conn = realUrl.openConnection();
			conn.setConnectTimeout(50000);//连接超时时间
			conn.setReadTimeout(50000);
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
			}
		} catch (Exception e) {
			logger.error("get error : ", e);
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

	public static String sendGet(String url, String param) throws Exception {
		logger.info("S : url = {}, param = {}", url, param);
		String result = "";
		BufferedReader in = null;
		try {
			String urlName = url + "?" + param;
			URL realUrl = new URL(urlName);
			// 打开和URL之间的链接
			URLConnection conn = realUrl.openConnection();
			conn.setConnectTimeout(50000);//连接超时时间
			conn.setReadTimeout(50000);
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
			}
		} catch (Exception e) {
			logger.error("get error : ", e);
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
	
	public static String sendGetPT(Map<String,Object> param) throws Exception{
		HttpsURLConnection connection = null;
		InputStream in = null;
		String result = "";
		try {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			URL certificateUrl = new File(param.get("certificate").toString()).toURI().toURL();
			File file = new File(certificateUrl.getFile());
			FileInputStream fis = new FileInputStream(file);
			ks.load(fis, param.get("crt_pwd").toString().toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, param.get("crt_pwd").toString().toCharArray());
			KeyManager[] kms = kmf.getKeyManagers();
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] certs,String authType) throws CertificateException {}
				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] certs,String authType) throws CertificateException {}
			} };

			HostnameVerifier allHostsValid = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			SSLContext sslContext = null;
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kms, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
			connection = (HttpsURLConnection) new URL(param.get("pt_url").toString()).openConnection();
			connection.setConnectTimeout(30000);connection.setReadTimeout(30000); 
			connection.setRequestProperty("X_ENTITY_KEY",param.get("entity_key").toString());
			in = connection.getInputStream();
			result = IOUtils.toString(in);
			logger.info("pt sendGet {},result = {}",param.get("pt_url"),result);
		} catch (Exception e) {
			logger.error("pt get error : ", e);
			throw e;
		}
		return result;
	}

	/**
	 * 判断是否是数字
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[-\\+]?[0-9]*");
		return pattern.matcher(str).matches();
	}

	/**
	 * 判断是否为空,反之!
	 * @param str
	 * @return
	 */
	public static boolean isNull(String str) {
		return (str == null || "".equals(str.trim()) || "null".equals(str.trim())) ? true : false;
	}

	public static Map<String, Object> isNull(Map<String, Object> resultMap) {
		Set<Entry<String, Object>> entrySet = resultMap.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			if (isNull(entry.getValue() + "")) {
				resultMap.clear();
				resultMap.put("status", "100099");
				resultMap.put("message", entry.getKey() + " is null");
				return resultMap;
			}
		}
		resultMap.clear();
		resultMap.put("status", "10000");
		resultMap.put("message", "success");
		return resultMap;
	}
}
