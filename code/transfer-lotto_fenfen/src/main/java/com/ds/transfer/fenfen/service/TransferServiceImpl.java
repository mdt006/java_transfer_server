package com.ds.transfer.fenfen.service;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.ds.msg.TelegramMessage;
import com.ds.transfer.common.service.CommonTransferService;
import com.ds.transfer.common.service.TransferService;
import com.ds.transfer.common.util.JSONUtils;
import com.ds.transfer.common.util.ReflectUtil;
import com.ds.transfer.common.util.StringsUtil;
import com.ds.transfer.common.vo.LoginParam;
import com.ds.transfer.common.vo.QueryBalanceParam;
import com.ds.transfer.common.vo.QueryOrderStatusParam;
import com.ds.transfer.common.vo.TransferParam;
import com.ds.transfer.common.vo.UserParam;
import com.ds.transfer.fenfen.constants.TelegramConstants;
import com.ds.transfer.fenfen.util.EncUtil;
import com.ds.transfer.fenfen.util.HMacMD5;
import com.ds.transfer.fenfen.util.Hex;
import com.ds.transfer.fenfen.util.PropsUtil;
import com.ds.transfer.fenfen.util.RSAUtil;
import com.ds.transfer.fenfen.vo.LoginVo;
import com.ds.transfer.record.entity.ApiInfoEntity;
import com.ds.transfer.record.entity.DsApiUserEntity;
import com.ds.transfer.record.service.TransferRecordService;

public class TransferServiceImpl extends CommonTransferService implements TransferService<DsApiUserEntity> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String DEFAULT_TRAY = "A";
	@Resource(name = "transferRecordService")
	private TransferRecordService transferRecordService;
	//小飞机消息组件
	TelegramMessage telegramMessage = TelegramMessage.getInstance();
		
	@Override
	public String transfer(TransferParam transferParam) {
		return null;
	}

	@Override
	@Deprecated
	public String queryBalance(QueryBalanceParam param) {
		return null;
	}
	
	/**
	 * 官方彩登录
	 * @param param
	 * @return
	 */
	public String login(LoginParam param){
		String loginAssignType = param.getLoginAssignType();
		if("login_guanfang".equals(loginAssignType)){
			return loginFenFenNew(param);
		}else{
			return loginFenFenOdd(param);
		}
	}
	
	/**
	 * 官方彩新登录方法
	 * @param param
	 * @return
	 */
	public String loginFenFenNew(LoginParam param) {
		StringBuffer result = new StringBuffer();
		StringBuilder message = new StringBuilder();
		Map<String,Object> messageMap = new HashMap<String,Object>();
		Map<String,Object> paramsMap = new HashMap<String,Object>();
		Map<String,Object> resultMap = new HashMap<String,Object>();
		message.append("DS loginFenFen 站点：").append(param.getEntity().getSiteId());
		message.append(",会员：").append(param.getUsername());
		
		try {
			ApiInfoEntity entity = param.getEntity();
			int isDemo = entity.getIsDemo() == 1 ? 0 : 1;
			String userName = param.getUsername();
			int siteId = entity.getSiteId();
			String accType = param.getAccType();
			String lottoTray = StringsUtil.isNull(param.getLottoTray()) ? DEFAULT_TRAY : param.getLottoTray();
			String clientType = param.getLottoType().equals("MP") ? "wap" : "pc";
			String lid = StringsUtil.isNull(param.getLid())== true?"0":param.getLid();
			String platformURL = param.getPlatformURL();
			String loginUrl = param.getLoginUrl();
			
			long time = System.currentTimeMillis();
			LoginVo vo = new LoginVo(userName,siteId,accType,lottoTray,isDemo,time);
			String url =  PropsUtil.getProperty("new_apiUrl");
			logger.info("fenfen login param:[username:"+userName+",siteId:"+siteId+",accType:"+accType+",lottoTray:"+lottoTray+",isDemo:"+isDemo+",time:"+time+"]");
			String loginParam = Base64.encodeBase64String(RSAUtil.encryptByPublicKey(JSONObject.toJSONString(vo).getBytes())).replace("\n", "");
			logger.info("fenfen login encrypt param:"+loginParam);
			String encrypt = generateNewKey(vo);
				
			paramsMap.put("encrypt", encrypt);
			paramsMap.put("param", loginParam);
			paramsMap.put("r", String.valueOf(System.currentTimeMillis()));
			
			messageMap.put("lid", lid);
			messageMap.put("platformURL", StringsUtil.isNull(platformURL)==true? "" : platformURL);
			messageMap.put("url",StringsUtil.isNull(loginUrl) == true ? Arrays.asList(url):Arrays.asList(loginUrl));
			messageMap.put("params", paramsMap);
			messageMap.put("clientType", clientType);
			
			resultMap.put("message", messageMap);
			resultMap.put("status", SUCCESS);
			logger.info("login fenfen result = {}", JSONUtils.map2Json(resultMap));
			
			return JSONUtils.map2Json(resultMap);
			
		} catch (Exception e) {
			message.insert("DS loginFenFen".length(),"登陆异常！").append("\n");
			message.append("异常信息：").append(e.getMessage());
			telegramMessage.sendMessage(TelegramConstants.BOT_KEY,TelegramConstants.TG_ID,"transfer-fenfen loginFenFen",message.toString().replace("&", "*"));
			logger.error("分分彩登录错误 : ", e);
			return JSONUtils.map2Json(maybe("系统内部出错 : " + result));
		}
	}
	
	/**
	 * 官方彩原有登录方法
	 * @param param
	 * @return
	 */
	public String loginFenFenOdd(LoginParam param) {
		String result = null;
		try {
			ApiInfoEntity entity = param.getEntity();
			result = "1".equals(entity.getIsDemo() + "") ? "2" : "3";
			LoginVo vo = new LoginVo(result, param.getLine(), System.currentTimeMillis() + "", param.getUsername(),//
					entity.getSiteId() + "", param.getAccType(), param.getPageSite(), StringsUtil.isNull(param.getLottoTray()) ? DEFAULT_TRAY : param.getLottoTray(), //
					param.getLottoType().equals("MP")  || param.getLottoType().equals("m")  ? "m" : "p");
			
			result = ReflectUtil.generateParam(vo);
			String key = this.generateKey(vo, param.getAction());
			String url = "2".equals(vo.getDcUserType()) ? PropsUtil.getProperty("odd_apiUrl") : PropsUtil.getProperty("odd_testApiUrl");
			logger.info("result = {}, key = {}, encrypt = {}", result, param.getAction(), key);
			result = url += "&" + result + "&dcEncrypt=" + key;
			return JSONUtils.map2Json(success(result));
		} catch (Exception e) {
			logger.error("分分彩登录错误 : ", e);
			return JSONUtils.map2Json(maybe("系统内部出错 : " + result));
		}
	}
	
	
	/**
	 * 生成分分彩最新登录加密串
	 */
	private String generateNewKey(LoginVo enVo){
		logger.info("SHA1加密前参数:"+enVo.getU()+enVo.getSi()+enVo.getUt()+enVo.getP()+enVo.getIt()+enVo.getTs()+PropsUtil.getProperty("loginKey"));
		return EncUtil.SHA1(enVo.getU()+enVo.getSi()+enVo.getUt()+enVo.getP()+enVo.getIt()+enVo.getTs()
				+PropsUtil.getProperty("loginKey"));
	}

	/**
	 * 生成分分彩加密串
	 */
	private String generateKey(LoginVo vo, String key) {
		try {
			String data = "dcCustomerId=" + vo.getDcCustomerId() + "&dcToken=" + vo.getDcToken() + //
					"&dcUsername=" + vo.getDcUsername() + "&dcSiteId=" + vo.getDcSiteId();
			String enc = Hex.byte2HexStr(HMacMD5.getHmacMd5Bytes(key.getBytes(), data.getBytes()));
			return enc;
		} catch (NoSuchAlgorithmException e) {
			logger.error("加密出错 : ", e);
		}
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
		// TODO Auto-generated method stub
		return null;
	}

}
