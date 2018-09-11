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
@RequestMapping("/wx")
public class WxAuthController {

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
        if (!wxService.checkSignature(signature, timestamp, nonce)) {
            return "非法请求";
        }
        return echostr;
    }

    @GetMapping("/getAccessToken")
    public String getAccessToken() {
        return wxService.getAccessToken();
    }

    @ResponseBody
    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(@RequestBody String requestBody, @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp, @RequestParam("nonce") String nonce) throws Exception {
        if (!wxService.checkSignature(signature, timestamp, nonce)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }

       String out = wxService.handleMsg(requestBody);

        System.out.println(requestBody);
        String result = "<![CDATA[服务正在准备]]>";
        String toUser = null;
        try {
            toUser  = requestBody.subSequence(requestBody.indexOf("<FromUserName>"),requestBody.indexOf("</FromUserName>")).toString().replace("<FromUserName>","");
            String content = requestBody.subSequence(requestBody.indexOf("<Content>"),requestBody.indexOf("</Content>")).toString().replace("<Content>","");
            if (content.equals("<![CDATA[购买]]>")) {
                result = "<![CDATA[客官别急，服务还需几天准备]]>";
            }
        } catch (Exception e) {
            result = "<![CDATA[服务正在准备]]>";
        }

        return "<xml> <ToUserName>"+toUser+"</ToUserName> <FromUserName><![CDATA[gh_6801fae6a84b]]></FromUserName> <CreateTime>12345678</CreateTime> <MsgType><![CDATA[text]]></MsgType> <Content>"+result+"</Content> </xml>";
    }




}

