package com.xiaosa.wx.controller;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderImpl;
import com.xiaosa.wx.service.WxService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
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

    @ResponseBody
    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(HttpServletRequest request,@RequestBody String requestBody, @RequestParam("signature") String signature,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature,
                       @RequestParam("timestamp") String timestamp, @RequestParam("nonce") String nonce) throws Exception {
        if (!checkSignature(signature, timestamp, nonce, token)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }
        System.out.println(requestBody);
        String result = "<![CDATA[爱你 ]]>";
        String toUser = null;
        try {
            String content = requestBody.subSequence(requestBody.indexOf("<Content>"),requestBody.indexOf("</Content>")).toString().replace("<Content>","");
            if (content.equals("<![CDATA[老公]]>")) {
                result = "<![CDATA[宝宝，I LOVE YOU ]]>";
            }
            toUser  = requestBody.subSequence(requestBody.indexOf("<FromUserName>"),requestBody.indexOf("</FromUserName>")).toString().replace("<FromUserName>","");
        } catch (Exception e) {
            result = "<![CDATA[回复老公，得惊喜]]>";
        }

        return "<xml> <ToUserName>"+toUser+"</ToUserName> <FromUserName><![CDATA[gh_2a542f784315]]></FromUserName> <CreateTime>12345678</CreateTime> <MsgType><![CDATA[text]]></MsgType> <Content>"+result+"</Content> </xml>";
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

