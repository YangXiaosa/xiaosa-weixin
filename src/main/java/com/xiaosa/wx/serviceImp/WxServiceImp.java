package com.xiaosa.wx.serviceImp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaosa.wx.model.WxAccessToken;
import com.xiaosa.wx.service.WxService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WxServiceImp implements WxService {

    private final static Logger LOGGER = LoggerFactory.getLogger(WxServiceImp.class);

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

}
