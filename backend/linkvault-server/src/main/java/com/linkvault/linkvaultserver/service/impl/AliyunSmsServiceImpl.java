package com.linkvault.linkvaultserver.service.impl;

import com.linkvault.linkvaultserver.config.AliyunSmsProperties;
import com.linkvault.linkvaultserver.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AliyunSmsServiceImpl implements SmsService {

    private final AliyunSmsProperties aliyunSmsProperties;

    @SuppressWarnings("unchecked")
    @Override
    public boolean sendSmsCode(String phone, String code) {
        if (!aliyunSmsProperties.isEnabled()) {
            log.info("[Mock短信服务] 验证码模拟下发成功 -> 手机号: {}, 验证码: {}", phone, code);
            return true;
        }

        String accessKeyId = aliyunSmsProperties.getAccessKeyId();
        String accessKeySecret = aliyunSmsProperties.getAccessKeySecret();

        if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
            log.error("[阿里云短信认证] 发送失败：未配置 AccessKeyID 或 AccessKeySecret");
            return false;
        }

        try {
            log.info("[阿里云短信认证] 开始发送短信认证验证码 -> 手机号: {}, 验证码: {}", phone, code);

            // 初始化阿里云 OpenAPI Config
            com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret);
            config.endpoint = aliyunSmsProperties.getEndpoint();

            com.aliyun.teaopenapi.Client client = new com.aliyun.teaopenapi.Client(config);

            // 配置接口参数 ( SendSmsVerifyCode )
            com.aliyun.teaopenapi.models.Params params = new com.aliyun.teaopenapi.models.Params()
                    .setAction("SendSmsVerifyCode")
                    .setVersion("2017-05-25")
                    .setProtocol("HTTPS")
                    .setMethod("POST")
                    .setAuthType("AK")
                    .setStyle("RPC")
                    .setPathname("/")
                    .setReqBodyType("json")
                    .setBodyType("json");

            Map<String, String> queries = new HashMap<>();
            queries.put("PhoneNumber", phone);
            queries.put("SignName", aliyunSmsProperties.getSignName());
            queries.put("TemplateCode", aliyunSmsProperties.getTemplateCode());
            queries.put("TemplateParam", "{\"code\":\"" + code + "\",\"min\":\"5\"}");

            com.aliyun.teaopenapi.models.OpenApiRequest request = new com.aliyun.teaopenapi.models.OpenApiRequest()
                    .setQuery(queries);

            Map<String, Object> response = (Map<String, Object>) (Map<?, ?>) client.callApi(params, request, new com.aliyun.teautil.models.RuntimeOptions());

            if (response != null && response.containsKey("body")) {
                Object bodyObj = response.get("body");
                if (bodyObj instanceof Map) {
                    Map<?, ?> bodyMap = (Map<?, ?>) bodyObj;
                    String responseCode = String.valueOf(bodyMap.get("Code"));
                    if ("OK".equalsIgnoreCase(responseCode)) {
                        log.info("[阿里云短信认证] 发送成功！已成功扣除套餐包次数");
                        return true;
                    } else {
                        log.error("[阿里云短信认证] 发送失败, Code={}, Message={}", responseCode, bodyMap.get("Message"));
                        return false;
                    }
                }
            }

            log.info("[阿里云短信认证] 接口已调用完成");
            return true;

        } catch (Exception e) {
            log.error("[阿里云短信认证] 异常导致短信发送失败", e);
            return false;
        }
    }
}
