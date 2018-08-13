package com.xiaosa.wx.service;

public interface WxService {
    String WX_API_URL = "https://api.weixin.qq.com";

    String getAccessToken();

    boolean checkSignature(String signature, String timestmp, String nonce);
}
