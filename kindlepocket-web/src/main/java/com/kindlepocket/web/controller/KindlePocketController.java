package com.kindlepocket.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kindlepocket.web.pojo.HttpResult;
import com.kindlepocket.web.pojo.Subscriber;
import com.kindlepocket.web.service.MailMessageSendService;
import com.kindlepocket.web.service.SubscriberService;
import com.kindlepocket.web.service.TextBookService;
import com.kindlepocket.web.util.CheckUtil;
import com.kindlepocket.web.util.MessageUtil;

@Controller
@RequestMapping("/KindlePocket")
public class KindlePocketController {

	private static final long serialVersionUID = 1L;

	private static String SUBSCRIBER_OPENID = null;

	private static final String BINDED = "1";
	private static final String MENU_ITEM_STRING_ONE = "1";
	private static final String MENU_ITEM_STRING_TWO = "2";
	private static final String MENU_ITEM_INIT_ENG = "menu";
	private static final String MENU_ITEM_INIT_CHI = "菜单";

	@Autowired
	private TextBookService bookService;

	@Autowired
	private SubscriberService ssbService;

	@Autowired
	private MailMessageSendService mailMessageSendService;

	private static Logger logger = Logger.getLogger(KindlePocketController.class);

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@RequestMapping("/homepage")
	public String toIndex() {
		if (logger.isInfoEnabled()) {
			logger.info("redirecting to homePage...");
		}
		return "index";
	}

	@RequestMapping("/vue")
	public String toVuePage() {
		return "vue";
	}

	@RequestMapping("/toDeliveryRecords")
	public String toInfoDetail() {
		if (logger.isInfoEnabled()) {
			logger.info("redirecting to delivery records page...");
		}
		return "deliveryRecords";
	}

	@RequestMapping("/toDetailsPage")
	public String toSearchDetailPage(@RequestParam("single") String single, @RequestParam("idList") String idList,
			@RequestParam("queryParam") String queryParam, HttpServletRequest request, HttpServletResponse response) {
		if (logger.isInfoEnabled()) {
			logger.info("redirecting to details page...");
		}
		Cookie cookie = null;
		Cookie temp = null;
		if (single.equals("false")) {
			try {
				cookie = new Cookie("queryParam", URLEncoder.encode(queryParam, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				if (logger.isErrorEnabled()) {
					logger.error("encode queryParam: [" + queryParam + "] encounterd problems!");
				}
			}
			temp = new Cookie("idList", null);
		} else {
			cookie = new Cookie("idList", idList);
			temp = new Cookie("queryParam", null);
		}
		cookie.setMaxAge(Integer.MAX_VALUE);
		cookie.setPath("/");
		temp.setMaxAge(0);
		temp.setPath("/");
		response.addCookie(cookie);
		response.addCookie(temp);
		return "details";
	}

	@RequestMapping("/getDetails")
	@ResponseBody
	public String getDetails(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		JsonNode result = null;
		if (!(null == cookies)) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("idList")) {
					// query single book with id
					String idList = cookie.getValue();
					if (logger.isInfoEnabled()) {
						logger.info("idList: " + idList);
					}
					Map<String, Object> queryMap = new HashMap<String, Object>();
					queryMap.put("key", "id");
					queryMap.put("value", idList);
					result = this.bookService.search(queryMap);
					if (logger.isInfoEnabled()) {
						logger.info("query single result:" + result);
					}
					break;
				} else if (cookie.getName().equals("queryParam")) {
					// query all the matched books
					String queryParam = cookie.getValue();
					Map<String, Object> queryMap = new HashMap<String, Object>();
					queryMap.put("key", "title");
					try {
						queryMap.put("value", URLDecoder.decode(queryParam, "utf-8"));
					} catch (UnsupportedEncodingException e) {
						if (logger.isErrorEnabled()) {
							logger.error("decode queryParam: [" + "] encountered problems!");
						}
					}
					result = this.bookService.search(queryMap);
					if (logger.isInfoEnabled()) {
						logger.info("query all the matched books:" + result);
					}
					break;
				}
			}
			return result.toString();
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("no cookies received");
			}
			return null;
		}
	}

	@RequestMapping("/testToBindingPage")
	public String testToBindingPage(Model model) {
		model.addAttribute("testKey", "testValue");
		return "register";
	}

	@RequestMapping("/testToInfoUpdate")
	public String testToInfoUpdate() {
		return "infoUpdate";
	}

	/**
	 *
	 * @param request
	 * @param response
	 * @param subscriberOpenId
	 * @param isBinded
	 * @param model
	 * @return
	 */
	@RequestMapping("/toBindingPage")
	public String toBindingPage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "subscriberOpenId", required = false) String subscriberOpenId, 
			@RequestParam(value = "isBinded", required = false) String isBinded,
			Model model) {

		// model.addAttribute("subscriberOpenId",subscriberOpenId);
		if(null != subscriberOpenId && !subscriberOpenId.isEmpty()) {
			Cookie cookie = new Cookie("subscriberOpenId", subscriberOpenId);
			cookie.setPath("/");
			cookie.setMaxAge(Integer.MAX_VALUE);
			response.addCookie(cookie);
		}
		model.addAttribute("subscriberOpenId", subscriberOpenId);

		if (null != isBinded && !isBinded.isEmpty() && isBinded.equalsIgnoreCase("true")) {
			if (logger.isInfoEnabled()) {
				logger.info("redirecting to info update page; openId:" + subscriberOpenId + " isBinded:" + isBinded);
			}
			return "infoUpdate";
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("redirecting to binding page; openId:" + subscriberOpenId + " isBinded:" + isBinded);
			}
			return "register";
		}

	}

	@RequestMapping(value = "/bindingData")
	@ResponseBody
	public String bindingData(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("phone") String phone, @RequestParam("userName") String userName,
			@RequestParam("email") String email, @RequestParam("emailPwd") String emailPwd,
			@RequestParam("kindleEmail") String kindleEmail) {
		if (logger.isInfoEnabled()) {
			logger.info("phone:" + phone + "userName" + userName + " email:" + email + " emailPwd:" + emailPwd
					+ " kindleEmail:" + kindleEmail);
		}
		String info = null;
		
		Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (Cookie cookie : cookies) {

				System.out.println("cookieName:" + cookie.getName() + " cookieValue:" + cookie.getValue());

				String subscriberOpenIdKey = cookie.getName();
				String subscriberOpenId = cookie.getValue();
				if (subscriberOpenIdKey.equalsIgnoreCase("subscriberOpenId")) {
					this.ssbService.binding(subscriberOpenId, phone, userName, email, emailPwd, kindleEmail);
					if (logger.isInfoEnabled()) {
						logger.info("subscriber: " + subscriberOpenId + " has binded information!");
					}
					if(this.isBinded(subscriberOpenId)) {
						info = this.ssbService.findSubscriberInfo(subscriberOpenId).getBody();
					}
				} else {
					if (logger.isInfoEnabled()) {
						logger.info("no valid subscriber information received!");
					}
				}
			}
		} else {
			System.out.println("no cookie received");
		}
		return info;
	}

	@RequestMapping(value = "/inPocket.do", method = RequestMethod.GET)
	public void validate(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("signature") String signature, @RequestParam("timestamp") String timestamp,
			@RequestParam("nonce") String nonce, @RequestParam("echostr") String echostr) {
		// http://33051bbe.nat123.net/Weixin/wx.do
		if (logger.isInfoEnabled()) {
			logger.info("\n***The message got: signature:" + signature + " timestamp:" + timestamp + " nonce:" + nonce
					+ " echostr:" + echostr);
		}

		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("response error");
			}
		}

		if (CheckUtil.checkSignature(signature, timestamp, nonce)) {
			out.print(echostr);
			if (logger.isInfoEnabled()) {
				logger.info("validated!");
			}
		}
	}

	@RequestMapping(value = "/inPocket.do", method = RequestMethod.POST)
	public void processMessage(HttpServletRequest request, HttpServletResponse response) throws IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {

			Map<String, String> map = MessageUtil.xmlToMap(request);
			String fromUserName = map.get("FromUserName");
			String toUserName = map.get("ToUserName");
			String msgType = map.get("MsgType");
			String content = null == map.get("Content") ? null : map.get("Content").trim();

			this.SUBSCRIBER_OPENID = fromUserName;

			if (logger.isInfoEnabled()) {
				logger.info("\n***The message got: fromUserName:" + fromUserName + " toUserName:" + toUserName
						+ " msgType:" + msgType + " content:" + content);
			}

			String responseMessage = null;
			if (MessageUtil.MESSAGE_TEXT.equals(msgType)) {

				switch (content) {
				case MENU_ITEM_STRING_ONE:
					responseMessage = MessageUtil.initSinglePicTextMessage(toUserName, fromUserName, "kindlePocket",
							"kindle text books sharing platform", "/imgs/welcome.jpg", "/KindlePocket/binding");
					break;
				case MENU_ITEM_STRING_TWO:
					Boolean isBinded = false;
					if (isBinded(fromUserName)) {
						isBinded = true;
					}
					responseMessage = MessageUtil.initSinglePicTextMessage(toUserName, fromUserName, "绑定步骤",
							"点击绑定kindle; \n输入\"menu\"或\"菜单\"查看更多选项", "/imgs/welcome.jpg",
							"/KindlePocket/toBindingPage?subscriberOpenId=" + fromUserName + "&isBinded=" + isBinded);
					break;
				case MENU_ITEM_INIT_ENG:
				case MENU_ITEM_INIT_CHI:
					responseMessage = MessageUtil.initText(toUserName, fromUserName, MessageUtil.menuText());
					break;
				default:
					// responseMessage = MessageUtil.initText(toUserName,
					// fromUserName, MessageUtil.menuText());
					// List<String> titleList =
					// this.bookService.search(content);
					Map<String, Object> queryMap = new HashMap<String, Object>();
					queryMap.put("key", "title");
					queryMap.put("value", content);
					JsonNode result = this.bookService.search(queryMap);
					List<String> titleList = new ArrayList<String>();
					List<String> idList = new ArrayList<String>();
					Iterator<JsonNode> iterator = result.iterator();
					JsonNode book = null;
					while (iterator.hasNext()) {
						book = iterator.next();
						titleList.add(book.get("title").toString());
						idList.add(book.get("id").toString());
					}
					logger.info("找到" + titleList.size() + "本书籍");
					responseMessage = MessageUtil.initSearchResultsPicTextMessage(toUserName, fromUserName, titleList,
							idList, content);
					break;
				}

				/*
				 * TextMessage textMessage = new TextMessage();
				 * textMessage.setFromUserName(toUserName);
				 * textMessage.setToUserName(fromUserName);
				 * textMessage.setMsgType("text"); textMessage.setCreateTime(new
				 * Date().getTime());
				 * textMessage.setContent("the message you sent was : " +
				 * content); responseMessage =
				 * MessageUtil.textMessageToXml(textMessage); if
				 * (logger.isInfoEnabled()) {
				 * logger.info("the message responsed is :\n" +
				 * responseMessage); }
				 */
			} else if (MessageUtil.MESSAGE_EVENT.equals(msgType)) {
				String eventType = map.get("Event");
				if (MessageUtil.MESSAGE_SUBSCRIBE.equals(eventType)) {
					this.ssbService.add(fromUserName);
					responseMessage = MessageUtil.initText(toUserName, fromUserName, MessageUtil.welcomeText());

				} else if (MessageUtil.MESSAGE_UNSUBSCRIBE.equals(eventType)) {
					this.ssbService.remove(fromUserName);
				}
			}
			if (logger.isInfoEnabled()) {
				logger.info("\n***The message responsed: \n" + responseMessage);
			}
			out.print(responseMessage);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	private Boolean isBinded(String subscriberOpenId) {
		HttpResult result = this.ssbService.findSubscriberInfo(subscriberOpenId);
		if (!StringUtils.isEmpty(result.getBody())) {
			try {
				JsonNode info = MAPPER.readTree(result.getBody());
				if (info.get("isBinded").toString().equals(BINDED)) {
					if (logger.isInfoEnabled()) {
						logger.info("the user has binded");
					}
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return false;
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("the user has not binded");
			}
			return false;
		}
	}

	@RequestMapping(value = "/getSubscriberInfo", method = RequestMethod.POST)
	@ResponseBody
	private String getSubscriberInfo(@RequestParam("subscriberOpenId") String subscriberOpenId,
			HttpServletResponse response) {
		if (logger.isInfoEnabled()) {
			logger.info("search for subscriber info; openId: " + subscriberOpenId);
		}
		HttpResult result = this.ssbService.findSubscriberInfo(subscriberOpenId);
		if (logger.isInfoEnabled()) {
			logger.info("searching result: " + result);
		}
		// JsonNode node = MAPPER.readTree(result.toString());
		// response.setHeader("Access-Control-Allow-Origin", "*");
		try {
			JsonNode node = MAPPER.readTree(result.getBody());
			System.out.println("node:" + node.toString());
			return result.getBody();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/updateSubscriberInfo", method = RequestMethod.POST)
	private String updateSubscriberInfo(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("subscriberOpenId") String openId, @RequestParam("phone") String phone,
			@RequestParam("userName") String userName, @RequestParam("email") String email,
			@RequestParam("emailPwd") String emailPwd, @RequestParam("kindleEmail") String kindleEmail) {
		if (logger.isInfoEnabled()) {
			logger.info("openId:" + openId + " phone:" + phone + " userName" + userName + " email:" + email
					+ " emailPwd:" + emailPwd + " kindleEmail:" + kindleEmail);
			Boolean updated = this.ssbService.updateSubscriberInfo(openId, phone, userName, email, emailPwd,
					kindleEmail);
		}
		return null;
	}

	@RequestMapping(value = "/getSubscriberOpenId", method = RequestMethod.GET)
	@ResponseBody
	public String getSubscriberOpenid(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (Cookie cookie : cookies) {

				System.out.println("cookieName:" + cookie.getName() + " cookieValue:" + cookie.getValue());

				String subscriberOpenIdKey = cookie.getName();
				String subscriberOpenId = cookie.getValue();
				if (subscriberOpenIdKey.equals("subscriberOpenId")) {
					if (logger.isInfoEnabled()) {
						logger.info("got subscriber openId: " + subscriberOpenId);
					}
					return subscriberOpenId;
				} else {
					if (logger.isInfoEnabled()) {
						logger.info("no valid subscriber information received!");
					}
					return null;
				}
			}
			return null;
		} else {
			System.out.println("no cookie received");
			return null;
		}
	}

	@RequestMapping(value = "/sendMailMessage", method = RequestMethod.GET)
	@ResponseBody
	public void sendMailMessage(HttpServletRequest request, @RequestParam("bookId") String bookId) {
		System.out.print("entered sendMail function\n");
		Cookie[] cookies = request.getCookies();
		String subscriberOpenId = null;
		if (null != cookies) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("subscriberOpenId")) {
					subscriberOpenId = cookie.getValue();
					if (logger.isInfoEnabled()) {
						logger.info(
								"ready to send the book; bookId:" + bookId + " subscriberOpenId:" + subscriberOpenId);
					}
					// this.bookService.sendMail(bookId,subscriberOpenId);
					Map<String, String> params = new HashMap<String, String>();
					params.put("bookId", bookId);
					params.put("subscriberOpenId", subscriberOpenId);
					try {
						String paramsStr = MAPPER.writeValueAsString(params);
						if (logger.isInfoEnabled()) {
							logger.info("sendMailMessage params: " + paramsStr);
						}
						this.mailMessageSendService.sendMsg(paramsStr);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			System.out.print("no cookie received");
		}
	}

	@RequestMapping(value = "/getDeliveryRecords", method = RequestMethod.GET)
	@ResponseBody
	public String getDeliveryRecords(HttpServletRequest request) {
		System.out.println("entered the function getDeliveryRecords");
		Cookie[] cookies = request.getCookies();
		String subscriberOpenId = null;
		HttpResult result = null;
		if (null != cookies) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("subscriberOpenId")) {
					subscriberOpenId = cookie.getValue();
					result = this.ssbService.findDeliveryRecords(subscriberOpenId);
					JsonNode node = null;
					try {
						node = MAPPER.readTree(result.getBody());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		} else {
			if (logger.isErrorEnabled()) {
				logger.error("no cookie received!");
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info("delivery record:" + result.getBody());
		}
		return result.getBody();
	}

	/*
	 * @RequestMapping(value="/sendMailMessage", method=RequestMethod.GET)
	 * public void sendMailMessage(@RequestParam("bookId")String bookId){
	 * this.mailMessageSendService.sendMsg("helloRabbit"); }
	 */
}
