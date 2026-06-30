package com.linkvault.linkvaultserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSmsProperties {
    /**
     * 是否开启真实短信发送
     */
    private boolean enabled = false;

    /**
     * 阿里云 AccessKey ID
     */
    private String accessKeyId;

    /**
     * 阿里云 AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 短信签名
     */
    private String signName;

    /**
     * 短信模板CODE
     */
    private String templateCode;

    /**
     * 服务 API 端点 Endpoint
     */
    private String endpoint = "dypnsapi.aliyuncs.com";
}
