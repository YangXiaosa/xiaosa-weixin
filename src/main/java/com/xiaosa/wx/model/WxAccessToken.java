package com.xiaosa.wx.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WxAccessToken {
    private String accessToken;
    private int expiresIn;
    private long expiresTimeStamp;

    private WxAccessToken(){}

    public static final WxAccessToken INSTANCE = new WxAccessToken();
}
