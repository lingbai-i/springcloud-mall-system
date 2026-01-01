package com.mall.sms.controller;

import com.mall.sms.dto.SendSmsRequest;
import com.mall.sms.dto.SmsResponse;
import com.mall.sms.dto.VerifySmsRequest;
import com.mall.sms.service.SmsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * SMS服务控制器
 *
 * @author lingbai
 * @since 2025-11-09
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    /**
     * 发送验证码
     *
     * @param request     发送请求
     * @param httpRequest HTTP请求
     * @return 发送结果
     */
    @PostMapping("/send")
    public SmsResponse sendVerificationCode(
            @Valid @RequestBody SendSmsRequest request,
            HttpServletRequest httpRequest) {

        log.info("收到发送验证码请求: phone={}, purpose={}", request.getPhoneNumber(), request.getPurpose());

        try {
            // 获取客户端IP
            String clientIp = getClientIp(httpRequest);

            // 发送验证码
            boolean success = smsService.sendVerificationCode(
                    request.getPhoneNumber(),
                    request.getPurpose(),
                    clientIp);

            if (success) {
                log.info("验证码发送成功: phone={}", request.getPhoneNumber());
                return SmsResponse.success("验证码发送成功");
            } else {
                log.warn("验证码发送失败: phone={}", request.getPhoneNumber());
                return SmsResponse.error("验证码发送失败");
            }
        } catch (Exception e) {
            log.error("发送验证码异常: phone={}, error={}", request.getPhoneNumber(), e.getMessage(), e);
            return SmsResponse.error("发送失败: " + e.getMessage());
        }
    }

    /**
     * 验证验证码
     *
     * @param request     验证请求
     * @param httpRequest HTTP请求
     * @return 验证结果
     */
    @PostMapping("/verify")
    public SmsResponse verifyCode(
            @Valid @RequestBody VerifySmsRequest request,
            HttpServletRequest httpRequest) {

        log.info("收到验证码验证请求: phone={}, purpose={}", request.getPhoneNumber(), request.getPurpose());

        try {
            // 获取客户端IP
            String clientIp = getClientIp(httpRequest);

            // 验证验证码
            boolean valid = smsService.verifyCode(
                    request.getPhoneNumber(),
                    request.getCode(),
                    request.getPurpose(),
                    clientIp);

            if (valid) {
                log.info("验证码验证成功: phone={}", request.getPhoneNumber());
                return SmsResponse.success("验证码正确");
            } else {
                log.warn("验证码验证失败: phone={}", request.getPhoneNumber());
                return SmsResponse.error("验证码错误或已过期");
            }
        } catch (Exception e) {
            log.error("验证验证码异常: phone={}, error={}", request.getPhoneNumber(), e.getMessage(), e);
            return SmsResponse.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户端真实IP
     *
     * @param request HTTP请求
     * @return 客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
