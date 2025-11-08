package com.mall.sms.controller;

import com.mall.sms.dto.SendSmsRequest;
import com.mall.sms.dto.SmsResponse;
import com.mall.sms.dto.VerifySmsRequest;
import com.mall.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 短信验证码控制器
 *
 * @author SMS Service
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    /**
     * 发送验证码
     */
    @PostMapping("/send")
    public SmsResponse<String> sendVerificationCode(
            @Valid @RequestBody SendSmsRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        log.info("发送验证码请求 - 手机号: {}, 用途: {}, IP: {}", 
                request.getPhoneNumber(), request.getPurpose(), clientIp);
        
        boolean result = smsService.sendVerificationCode(request.getPhoneNumber(), request.getPurpose(), clientIp);
        if (result) {
            return SmsResponse.success("验证码发送成功");
        } else {
            return SmsResponse.error("验证码发送失败");
        }
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify")
    public SmsResponse<String> verifyCode(
            @Valid @RequestBody VerifySmsRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        log.info("验证验证码请求 - 手机号: {}, 用途: {}, IP: {}", 
                request.getPhoneNumber(), request.getPurpose(), clientIp);
        
        boolean result = smsService.verifyCode(request.getPhoneNumber(), request.getCode(), request.getPurpose(), clientIp);
        if (result) {
            return SmsResponse.success("验证码验证成功");
        } else {
            return SmsResponse.error("验证码验证失败");
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public SmsResponse<String> health() {
        return SmsResponse.success("SMS服务运行正常");
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}