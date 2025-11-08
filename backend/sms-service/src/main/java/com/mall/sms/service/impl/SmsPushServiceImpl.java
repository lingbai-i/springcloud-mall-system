package com.mall.sms.service.impl;

import com.mall.sms.config.SmsProperties;
import com.mall.sms.service.SmsPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * SMS推送服务实现类
 *
 * @author SMS Service
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsPushServiceImpl implements SmsPushService {

    private final SmsProperties smsProperties;
    private final RestTemplate restTemplate;

    @Override
    public void sendSms(String phoneNumber, String code, String purpose) {
        try {
            // 构建请求参数 - 使用form-data格式
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("name", smsProperties.getServiceName());  // 推送助手名称
            requestBody.add("code", code);                            // 验证码
            requestBody.add("targets", phoneNumber);                  // 目标手机号
            requestBody.add("content", buildSmsMessage(code, purpose)); // 添加短信内容
            requestBody.add("number", "3");                           // 验证码有效时间（分钟），3分钟
            
            // 设置请求头 - 使用form-data格式
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            String url = smsProperties.getPush().getUrl();
            log.info("发送短信请求 - URL: {}, 手机号: {}, 验证码: {}, 用途: {}", url, phoneNumber, code, purpose);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("短信发送成功 - 手机号: {}, 用途: {}, 响应: {}", phoneNumber, purpose, response.getBody());
            } else {
                log.error("短信发送失败 - 手机号: {}, 用途: {}, 状态码: {}, 响应: {}", 
                        phoneNumber, purpose, response.getStatusCode(), response.getBody());
                throw new RuntimeException("短信发送失败，状态码: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("短信发送异常 - 手机号: {}, 用途: {}, 错误: {}", phoneNumber, purpose, e.getMessage(), e);
            throw new RuntimeException("短信发送失败: " + e.getMessage());
        }
    }

    /**
     * 构建短信内容
     */
    private String buildSmsMessage(String code, String purpose) {
        String purposeText = getPurposeText(purpose);
        return String.format("【%s】您的%s验证码是：%s，有效期3分钟，请勿泄露给他人。", 
                smsProperties.getServiceName(), purposeText, code);
    }

    /**
     * 获取用途文本
     */
    private String getPurposeText(String purpose) {
        switch (purpose.toLowerCase()) {
            case "login":
                return "登录";
            case "register":
                return "注册";
            case "reset_password":
                return "重置密码";
            case "bind_phone":
                return "绑定手机";
            case "change_phone":
                return "更换手机";
            default:
                return "验证";
        }
    }
}