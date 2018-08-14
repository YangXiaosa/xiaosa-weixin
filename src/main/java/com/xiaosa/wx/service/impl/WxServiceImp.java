package com.xiaosa.wx.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaosa.wx.model.WxAccessToken;
import com.xiaosa.wx.model.WxMsg;
import com.xiaosa.wx.service.WxService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

@Service
public class WxServiceImp implements WxService {

    private final static Logger LOGGER = LoggerFactory.getLogger(WxServiceImp.class);

    @Value("${token}")
    private String token;

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static int tryCount = 0;

    public String getAccessToken() {
        LOGGER.info("access getAccessToken function");
        if (tryCount > 10) {
            throw new RuntimeException("获取access_token失败！！！");
        }
        long expiresTimeStamp = WxAccessToken.INSTANCE.getExpiresTimeStamp();
        if (expiresTimeStamp > DateTime.now().getMillis()) {
            tryCount = 0;
            LOGGER.info("return result : {}", WxAccessToken.INSTANCE.getAccessToken());
            return WxAccessToken.INSTANCE.getAccessToken();
        } else {
            tryCount ++;
            OkHttpClient httpClient = new OkHttpClient();
            try {
                Request request = new Request.Builder().url(WX_API_URL + "/cgi-bin/token?grant_type=client_credential&appid=wxb427f89f4e349c42&secret=8bf3f3654affe5b6efcab5cfed09cfab").build();
                Response response = httpClient.newCall(request).execute();
                String resultJson = response.body().string();
                JsonNode node = objectMapper.readTree(resultJson);
                JsonNode accessTokenNode = node.get("access_token");
                JsonNode expiresInNode = node.get("expires_in");
                if (null != accessTokenNode && null != expiresInNode) {
                    String accessToken = accessTokenNode.asText();
                    int expiresIn = expiresInNode.asInt();
                    WxAccessToken.INSTANCE.setAccessToken(accessToken);
                    WxAccessToken.INSTANCE.setExpiresIn(expiresIn);
                    WxAccessToken.INSTANCE.setExpiresTimeStamp(DateTime.now().getMillis()+((expiresIn-600)*1000));
                } else {
                    return getAccessToken();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                return getAccessToken();
            }
        }
        return getAccessToken();
    }


    public boolean checkSignature(String signature, String timestmp, String nonce) {
        List<String> params = Arrays.asList(timestmp,nonce,token);
        Collections.sort(params);
        StringBuffer sb = new StringBuffer();
        for (String str : params) {
            sb.append(str);
        }
        String sha1Str = DigestUtils.sha1Hex(sb.toString());
        if (sha1Str.equals(signature)) {
            return true;
        }
        return false;
    }

    public String handleMsg(String xmlMsg) throws DocumentException{
//        WxMsg wxMsg = parseXmlToWxMsg(xmlMsg);
//        MsgHandler handler = HandlerMannager.getHandler(wxMsg);
//        return handler.handle(wxMsg);
        return null;
    }

    public WxMsg parseXmlToWxMsg(String xmlMsg) throws DocumentException {
        WxMsg wxMsg = new WxMsg();
        SAXReader reader = new SAXReader();
        StringReader sr =  new StringReader(xmlMsg);
        Document document = reader.read(sr);
        Element rootElement = document.getRootElement();
        wxMsg.setToUser(rootElement.element("ToUserName").getText());
        wxMsg.setFromUser(rootElement.element("FromUserName").getText());
        wxMsg.setCreateTime(Long.parseLong(rootElement.element("CreateTime").getText()));
        wxMsg.setMsgType(rootElement.element("MsgType").getText());

        String msgType = wxMsg.getMsgType();
        Map<String, String> content;
        if (MSG_TYPE_EVENT.equals(msgType)){
            content = parseEventTypeToMap(rootElement);
        }else {
            content = parseOtherTypeToMap(rootElement);
        }
        wxMsg.setContent(content);
        return wxMsg;
    }

    private Map<String, String> parseEventTypeToMap(Element root){
//        String event = root.element("Event").getText();
//        EventParseHandler handler = EventParseHandler.getHandler(event);
//        return handler.handle(root);
        return null;
    }
    private Map<String, String> parseOtherTypeToMap(Element root){
        return null;
    }
}
