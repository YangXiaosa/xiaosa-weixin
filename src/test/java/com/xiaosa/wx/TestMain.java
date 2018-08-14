package com.xiaosa.wx;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.StringReader;


public class TestMain {

    public static void main(String[] args) throws DocumentException {
        SAXReader reader = new SAXReader();
        String xml = "<xml> <ToUserName>xxxxxxxxxxxxxx</ToUserName> <FromUserName><![CDATA[gh_2a542f784315]]></FromUserName> <CreateTime>12345678</CreateTime> <MsgType><![CDATA[text]]></MsgType> <Content>xxxxxxxx</Content> </xml>";
        StringReader sr = new StringReader(xml);

        Document document = reader.read(sr);
        System.out.println(document);
    }
}
