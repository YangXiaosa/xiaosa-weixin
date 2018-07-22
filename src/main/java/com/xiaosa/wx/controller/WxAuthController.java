package com.xiaosa.wx.controller;

import com.xiaosa.wx.service.WxService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
public class WxAuthController {

    @Value("${token}")
    private String token;

    @Autowired
    private WxService wxService;

    @GetMapping("/")
    public String authSignature(HttpServletRequest request) {
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echostr = request.getParameter("echostr");
        if (signature == null || timestamp == null || nonce == null || echostr == null) {
            return "非法请求";
        }
        if (!checkSignature(signature, timestamp, nonce, token)) {
            return "非法请求";
        }
        return echostr;
    }

    private boolean checkSignature(String signature, String timestmp, String nonce, String token) {
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

    @GetMapping("/getAccessToken")
    public String getAccessToken() {
        return wxService.getAccessToken();
    }

}

