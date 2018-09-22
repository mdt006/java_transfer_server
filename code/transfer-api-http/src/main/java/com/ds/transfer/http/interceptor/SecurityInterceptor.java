package com.ds.transfer.http.interceptor;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.ds.transfer.common.util.JSONUtils;
import com.ds.transfer.http.constants.RemarkConstants;
import com.ds.transfer.http.controller.BaseController;
import com.ds.transfer.http.entity.DsIpList;
import com.ds.transfer.http.entity.DsIpListExample;
import com.ds.transfer.http.mapper.DsIpListMapper;
import com.ds.transfer.http.util.PropsUtil;
import com.ds.transfer.record.entity.TransferRemarkConfEntity;
import com.ds.transfer.record.entity.TransferRemarkConfEntityExample;
import com.ds.transfer.record.mapper.TransferRemarkConfEntityMapper;

/**
 * @ClassName: SecurityInterceptor
 * @Description: TODO(IP安全拦截器)
 * @author leo
 * @date 2018年5月23日
 */
@Component
public class SecurityInterceptor extends BaseController implements HandlerInterceptor {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public List<DsIpList> whiteList = new ArrayList<DsIpList>();
	@Autowired
	private DsIpListMapper dsIpListMapper;
	@Autowired
	private TransferRemarkConfEntityMapper transferRemarkMapper;

	@PostConstruct
	@Scheduled(cron = "${spring.schedule}")
	public void queryIpList() {
		DsIpListExample dsIpExample = new DsIpListExample();
		dsIpExample.createCriteria().andStateEqualTo(50);
		List<DsIpList> ipList = dsIpListMapper.selectByExample(dsIpExample);
		whiteList = ipList;
		logger.info("初始化白名单IP完成！");
	}
	
	@PostConstruct
	@Scheduled(cron = "${spring.schedule}")
	public void initTransferRemark(){
		try {
			TransferRemarkConfEntityExample transferRemark = new TransferRemarkConfEntityExample();
			transferRemark.createCriteria().andProjectEqualTo("transfer-api-http");
			List<TransferRemarkConfEntity> transferRemarkList = transferRemarkMapper.selectByExample(transferRemark);
			for (Iterator<TransferRemarkConfEntity> iterator = transferRemarkList.iterator(); iterator.hasNext();) {
				TransferRemarkConfEntity transferRemarkConfEntity = (TransferRemarkConfEntity) iterator.next();
				Field[] fields = RemarkConstants.class.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					fields[i].setAccessible(true);
					if(fields[i].getName().equals(transferRemarkConfEntity.getRemarkField())){
						fields[i].set(RemarkConstants.class, transferRemarkConfEntity.getContent());
					}
				}
			}
			logger.info("初始化转账中心备注完成！");
		} catch (Exception e) {
			logger.info("初始化转账中心备注异常！");
			e.printStackTrace();
		}
	}

	@Override
	public boolean preHandle(HttpServletRequest request,HttpServletResponse response, Object handler) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String ip = this.getIpAddr(request);
			if (Boolean.valueOf(PropsUtil.getProperty("ipValid"))) {
				if(whiteList !=null && whiteList.size()>0){
					for (int i = 0; i < whiteList.size(); i++) {
						DsIpList dsIpList = whiteList.get(i);
						if (!dsIpList.getIp().equals(ip)) {
							resultMap.clear();
							resultMap.put(STATUS, IP_NOT_ALLOW);
							resultMap.put(MESSAGE, "ip is not allowed");
						} else {
							return true;
						}
					}
				}else{
					resultMap.put(STATUS, IP_NOT_ALLOW);
					resultMap.put(MESSAGE, "ip is not allowed");
				}
				PrintWriter out = response.getWriter();
				out.write(JSONUtils.map2Json(resultMap));
				out.flush();
				logger.info("ip={} 不在白名单内，result={}",ip,JSONUtils.map2Json(resultMap));
			}
		} catch (Exception e) {
			logger.error("拦截器异常！",e);
		}
		return false;
	}

	/**
	 * @Title: batchCancelBetlotto
	 * @Package com.ds.lotto.controller
	 * @Description: TODO(获取客户端请求IP)
	 * @param @param request
	 * @param @param response
	 * @param @return 设定文件
	 * @return Object 返回类型
	 * @date: 2018年05月23日 下午2:46:54
	 * @author: leo
	 * @version V1.0
	 * @Copyright: 2018 鼎泰科技 Inc. All rights reserved.
	 *             注意：本内容仅限于鼎泰科技有限公司内部传阅，禁止外泄以及用于其他的商业目
	 */
	public String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null) {
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_CLIENT_IP");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}
		}
		return ip;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

	}
}
