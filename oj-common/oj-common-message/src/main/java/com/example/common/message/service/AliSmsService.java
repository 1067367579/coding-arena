package com.example.common.message.service;

import com.alibaba.fastjson2.JSON;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

//@Component
@Slf4j
public class AliSmsService {

    //@Autowired
    private Client aliCLient;

    //业务配置
    //@Value("${sms.aliyun.templateCode:}")
    private String templateCode;
    //@Value("${sms.send-message:true}")
    private boolean sendMessage;
    //@Value("${sms.sign-name:}")
    private String signName;

    /**
     * 发送短信验证码
     */
    public boolean sendMobileCode(String phone, String code) {
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        return sendTempMessage(phone, templateCode, params);
    }

    /**
     * 发送模板消息
     */
    public boolean sendTempMessage(String phone, String templateCode,
                                   Map<String, String> params) {
        if (!sendMessage) {
            log.error("短信发送通道关闭，发送失败......{}", phone);
            return false;
        }
        SendSmsRequest sendSmsRequest = new SendSmsRequest();
        sendSmsRequest.setPhoneNumbers(phone);
        sendSmsRequest.setSignName(signName);
        sendSmsRequest.setTemplateCode(templateCode);
        sendSmsRequest.setTemplateParam(JSON.toJSONString(params));
        try {
            SendSmsResponse sendSmsResponse =
                    aliCLient.sendSms(sendSmsRequest);
            SendSmsResponseBody responseBody = sendSmsResponse.getBody();
            if (!"OK".equalsIgnoreCase(responseBody.getCode())) {
                failReason(sendSmsRequest, responseBody.getMessage());
                return false;
            }
            return true;
        } catch (Exception e) {
            failReason(sendSmsRequest, e.getMessage());
            return false;
        }
    }

    private static void failReason(SendSmsRequest sendSmsRequest, String responseBody) {
        log.error("短信{} 发送失败，失败原因:{}.... ",
                JSON.toJSONString(sendSmsRequest), responseBody);
    }
}
