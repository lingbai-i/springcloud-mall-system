package com.mall.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 支付服务客户端
 * 用于调用支付服务的相关接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@FeignClient(name = "payment-service", path = "/api/payment")
public interface PaymentClient {
    
    /**
     * 创建支付订单
     * 
     * @param paymentRequest 支付请求
     * @return 支付结果
     */
    @PostMapping("/create")
    Map<String, Object> createPayment(@RequestBody Map<String, Object> paymentRequest);
    
    /**
     * 查询支付状态
     * 
     * @param paymentId 支付ID
     * @return 支付状态
     */
    @GetMapping("/{paymentId}/status")
    Map<String, Object> getPaymentStatus(@PathVariable("paymentId") String paymentId);
    
    /**
     * 取消支付
     * 
     * @param paymentId 支付ID
     * @return 取消结果
     */
    @PostMapping("/{paymentId}/cancel")
    Boolean cancelPayment(@PathVariable("paymentId") String paymentId);
    
    /**
     * 申请退款
     * 
     * @param refundRequest 退款请求
     * @return 退款结果
     */
    @PostMapping("/refund")
    Map<String, Object> refund(@RequestBody Map<String, Object> refundRequest);
    
    /**
     * 查询退款状态
     * 
     * @param refundId 退款ID
     * @return 退款状态
     */
    @GetMapping("/refund/{refundId}/status")
    Map<String, Object> getRefundStatus(@PathVariable("refundId") String refundId);
}