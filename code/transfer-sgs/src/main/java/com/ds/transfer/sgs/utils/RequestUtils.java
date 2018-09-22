package com.ds.transfer.sgs.utils;
import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestUtils {
	public static String sendPost(String url, Map<String, Object> params,String encoding) throws Exception{
		String result = "";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(50000)
				.setConnectionRequestTimeout(50000)
				.setSocketTimeout(50000)
				.setRedirectsEnabled(true).build();
		
		httpPost.setConfig(requestConfig);
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Authorization","SGS DTA:" + params.get("signature"));
		httpPost.setHeader("X-Sgs-Date", params.get("sgsDate").toString());
		
		try {
			httpPost.setEntity(new StringEntity(String.valueOf(params.get("requestParam")),ContentType.create("application/json", "utf-8")));
			HttpResponse response = httpClient.execute(httpPost);
			result = EntityUtils.toString(response.getEntity()); 
		} catch (Exception e) {
			throw e;
		} finally {
			if (null != httpClient) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	
	public static String sendGet(String url,Map<String,Object> params)throws Exception{
		String result  = "";
		
		CloseableHttpClient httpCilent = HttpClients.createDefault();
	    RequestConfig requestConfig = RequestConfig.custom()
	            .setConnectTimeout(5000)   //设置连接超时时间
	            .setConnectionRequestTimeout(5000) //设置请求超时时间
	            .setSocketTimeout(5000)
	            .setRedirectsEnabled(true)//默认允许自动重定向
	            .build();
	    
	    HttpGet httpGet = new HttpGet(url);
	    httpGet.setHeader("Authorization","SGS DTA:" + params.get("signature"));
	    httpGet.setHeader("X-Sgs-Date", params.get("sgsDate").toString());
	    httpGet.setConfig(requestConfig);
	    
	    try {
	        HttpResponse httpResponse = httpCilent.execute(httpGet);
	        result = EntityUtils.toString(httpResponse.getEntity());//获得返回的结果
	    } catch (Exception e) {
	        throw e;
	    }finally {
	        try {
	            httpCilent.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return result;
	}
}
