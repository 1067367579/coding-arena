package com.example.common.message.config;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;

//@Configuration
public class AliSmsConfig {

    //@Value("${sms.aliyun.accessKeyId:}")
    private String accessKeyId;
    //@Value("${sms.aliyun.accessKeySecret:}")
    private String accessKeySecret;
    //@Value("${sms.aliyun.endpoint:}")
    private String endpoint;

    //@Bean("aliClient")
    public Client client() throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint(endpoint);
        return new Client(config);
    }
}
