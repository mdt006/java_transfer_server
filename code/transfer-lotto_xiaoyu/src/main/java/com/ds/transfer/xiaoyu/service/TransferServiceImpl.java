package com.ds.transfer.xiaoyu.service;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ds.msg.TelegramMessage;
import com.ds.transfer.common.service.CommonTransferService;
import com.ds.transfer.common.service.TransferService;
import com.ds.transfer.common.util.JSONUtils;
import com.ds.transfer.common.util.StringsUtil;
import com.ds.transfer.common.vo.LoginParam;
import com.ds.transfer.common.vo.QueryBalanceParam;
import com.ds.transfer.common.vo.QueryOrderStatusParam;
import com.ds.transfer.common.vo.TransferParam;
import com.ds.transfer.common.vo.UserParam;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.DsApiUserEntity;
import com.ds.transfer.record.service.TransferRecordService;
import com.ds.transfer.xiaoyu.constants.TelegramConstants;
import com.ds.transfer.xiaoyu.util.EncUtil;
import com.ds.transfer.xiaoyu.util.PropsUtil;
import com.ds.transfer.xiaoyu.util.RSAUtil;
import com.ds.transfer.xiaoyu.vo.LoginEncryptVo;

public class TransferServiceImpl extends CommonTransferService implements TransferService<DsApiUserEntity> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;
	//小飞机消息组件
	TelegramMessage telegramMessage = TelegramMessage.getInstance();
		
	
	/**
	*@Title: batchCancelBetlotto 
	* @Package com.ds.lotto.controller
	* @Description: TODO(经典彩登录) 
	* @param @param LoginParam
	* @return String    返回类型 
	* @date: 2018年02月06日 下午2:46:54  
	* @author: leo 
	* @version V1.0
	* @Copyright: 2018 鼎泰科技 Inc. All rights reserved. 
	* 注意：本内容仅限于鼎泰科技有限公司内部传阅，禁止外泄以及用于其他的商业目
	*/
	@Override
	public String login(LoginParam param) {
		StringBuilder message = new StringBuilder();
		String result = null;
		try {
			Map<String,Object> messageMap = new HashMap<String,Object>();
			Map<String,Object> paramsMap = new HashMap<String,Object>();
			Map<String,Object> resultMap = new HashMap<String,Object>();
			ApiInfoEntity entity = param.getEntity();
			String loginType = param.getLoginAssignType().replace("login_", "");
			String loginUrl = param.getLoginUrl();
			String terminal = param.getTerminal();
			terminal = terminal.toLowerCase();
			
			message.append("xiaoyu loginJingDian 站点：").append(param.getEntity().getSiteId());
			message.append(",会员：").append(param.getUsername());
			
			Map<String, Object> localMap = JSONUtils.json2Map(entity.getReportUrl());
			JSONObject lottoURLInfo = (JSONObject)localMap.get(loginType);
			JSONArray lottoURlArr = (JSONArray)lottoURLInfo.get(terminal);
			
			LoginEncryptVo lev = new LoginEncryptVo();
			lev.setU(param.getUsername());
			lev.setSi(entity.getSiteId());
			lev.setUt(param.getAccType());
			lev.setP(param.getLottoTray());
			lev.setIt((1 == entity.getIsDemo().intValue()) ? 0 : 1);
			lev.setTs(System.currentTimeMillis());
			String jsonParams = JSONObject.toJSONString(lev);
			logger.info("jingdian login encrypt Before param = {}",jsonParams);
			//RSA加密
			byte[] RsaEncrypt = RSAUtil.encrypt(jsonParams);
			//BASE64加密
			String base64Str = Base64.encodeBase64String(RsaEncrypt).replace("\n", "");
			String loginParam = URLEncoder.encode(base64Str,"utf-8");
			String encrypt = generateJdKey(lev);
			
			paramsMap.put("encrypt", encrypt);
			paramsMap.put("param",URLDecoder.decode(loginParam,"UTF-8"));
			paramsMap.put("r", String.valueOf(System.currentTimeMillis()));
			
			messageMap.put("lid", StringsUtil.isNull(param.getLid())== true ? "0" : param.getLid());
			messageMap.put("platformURL",param.getPlatformURL());
			messageMap.put("url",StringsUtil.isNull(loginUrl)==true?lottoURlArr:Arrays.asList(loginUrl));
			messageMap.put("params", paramsMap);
			//加入query参数
			StringBuffer sb = new StringBuffer();
			sb.append("user=").append(param.getUsername()).append("&").append("siteId=").append(entity.getSiteId()).append("&");
			sb.append("istest=").append(entity.getIsDemo()).append("&").append("usertree=").append(param.getAccType()).append("&").append("pan=").append(param.getLottoTray());
			messageMap.put("query",sb.toString());

			resultMap.put("message", messageMap);
			resultMap.put("status", SUCCESS);
			logger.info("login jigndian siteId={},username={}, result = {}",
					entity.getSiteId(),param.getUsername(),JSONUtils.map2Json(resultMap));
			return JSONUtils.map2Json(resultMap);
		} catch (Exception e) {
			logger.error("经典彩登录错误 : ", e);
			message.insert("xiaoyu loginJingDian".length(),"登陆经典彩异常！");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-xiaoyu loginJingDian",message.toString().replace("&", "*"));
			return JSONUtils.map2Json(maybe("系统内部出错 : " + result));
		}
	}
	
	/**
	* @Title: batchCancelBetlotto 
	* @Package com.ds.lotto.controller
	* @Description: TODO(生成加密串) 
	* @param：  LoginEncryptVo
	* @return String    返回类型 
	* @date: 2018年02月07日 下午2:46:54  
	* @author: leo 
	* @version V1.0
	* @Copyright: 2018 鼎泰科技 Inc. All rights reserved. 
	* 注意：本内容仅限于鼎泰科技有限公司内部传阅，禁止外泄以及用于其他的商业目
	*/
	private String generateJdKey(LoginEncryptVo enVo){
		if(null != enVo){
			StringBuffer jdKey = new StringBuffer();
			jdKey.append(enVo.getU());
			jdKey.append(enVo.getSi());
			jdKey.append(enVo.getUt());
			jdKey.append(enVo.getP());
			jdKey.append(enVo.getIt());
			jdKey.append(enVo.getTs());
			jdKey.append(PropsUtil.getProperty("loginKey"));
			return EncUtil.SHA1(jdKey.toString());
		}
		logger.error("jingdian or xingyun lotto introduction param LoginEncryptVo is null !");
		return null;
	}
	
	@Override
	public String transfer(TransferParam transferParam) {
		return null;
	}
	
	@Override
	@Deprecated
	public String queryBalance(QueryBalanceParam param) {
		return null;
	}

	@Override
	public String loginBySingGame(LoginParam param) {
		return this.login(param);
	}
	
	@Override
	@Deprecated
	public DsApiUserEntity queryUserExist(String username) {
		return null;
	}

	@Override
	public Map<String, Object> checkAndCreateMember(UserParam param) {
		return null;
	}

	@Override
	public String queryStatusByBillno(QueryOrderStatusParam param) {
		return null;
	}

	@Override
	public String queryAgentBalance(QueryBalanceParam param) {
		return null;
	}
}
