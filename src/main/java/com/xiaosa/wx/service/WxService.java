package com.xiaosa.wx.service;

import org.dom4j.DocumentException;

public interface WxService {
    String WX_API_URL = "https://api.weixin.qq.com";

    String MSG_TYPE_EVENT = "event";

    String getAccessToken();

    boolean checkSignature(String signature, String timestmp, String nonce);

    String handleMsg(String xmlMsg)throws DocumentException;
}
