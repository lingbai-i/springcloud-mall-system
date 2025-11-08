package com.mall.payment.exception;

/**
 * 支付异常基类
 * 定义支付业务中的各种异常情况，提供统一的异常处理机制
 * 
 * <p>异常分类：</p>
 * <ul>
 *   <li>业务异常：参数错误、状态不匹配、余额不足等</li>
 *   <li>系统异常：网络超时、服务不可用、配置错误等</li>
 *   <li>第三方异常：支付平台返回的错误信息</li>
 *   <li>安全异常：签名验证失败、权限不足等</li>
 * </ul>
 * 
 * <p>异常处理：</p>
 * <ul>
 *   <li>错误码：统一的错误码体系，便于前端处理</li>
 *   <li>错误信息：用户友好的错误提示信息</li>
 *   <li>异常链：保留原始异常信息，便于调试</li>
 *   <li>日志记录：自动记录异常详情和堆栈信息</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // 抛出业务异常
 * throw new PaymentException("PAYMENT_001", "支付金额不能为空");
 * 
 * // 包装系统异常
 * throw new PaymentException("PAYMENT_002", "支付服务暂时不可用", cause);
 * </pre>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加异常分类和处理机制说明
 * V1.1 2024-12-15：增加错误码体系和异常链支持
 * V1.0 2024-12-01：初始版本，定义基本异常结构
 */
public class PaymentException extends RuntimeException {

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 错误详情
     */
    private final Object errorDetails;

    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public PaymentException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetails = null;
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param message 错误信息
     * @param cause 原因异常
     */
    public PaymentException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorDetails = null;
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param message 错误信息
     * @param errorDetails 错误详情
     */
    public PaymentException(String errorCode, String message, Object errorDetails) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param message 错误信息
     * @param errorDetails 错误详情
     * @param cause 原因异常
     */
    public PaymentException(String errorCode, String message, Object errorDetails, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
    }

    /**
     * 获取错误码
     * 
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误详情
     * 
     * @return 错误详情
     */
    public Object getErrorDetails() {
        return errorDetails;
    }

    // ==================== 常用异常工厂方法 ====================

    /**
     * 创建参数无效异常
     * 
     * @param message 错误信息
     * @return 支付异常
     */
    public static PaymentException invalidParameter(String message) {
        return new PaymentException("INVALID_PARAMETER", message);
    }

    /**
     * 创建订单不存在异常
     * 
     * @param orderId 订单ID
     * @return 支付异常
     */
    public static PaymentException orderNotFound(String orderId) {
        return new PaymentException("ORDER_NOT_FOUND", "支付订单不存在: " + orderId);
    }

    /**
     * 创建订单状态无效异常
     * 
     * @param orderId 订单ID
     * @param currentStatus 当前状态
     * @return 支付异常
     */
    public static PaymentException invalidOrderStatus(String orderId, String currentStatus) {
        return new PaymentException("INVALID_ORDER_STATUS", 
                                  String.format("订单状态无效，订单ID: %s, 当前状态: %s", orderId, currentStatus));
    }

    /**
     * 创建支付方式不支持异常
     * 
     * @param paymentMethod 支付方式
     * @return 支付异常
     */
    public static PaymentException unsupportedPaymentMethod(String paymentMethod) {
        return new PaymentException("UNSUPPORTED_PAYMENT_METHOD", "不支持的支付方式: " + paymentMethod);
    }

    /**
     * 创建支付金额无效异常
     * 
     * @param amount 支付金额
     * @return 支付异常
     */
    public static PaymentException invalidAmount(String amount) {
        return new PaymentException("INVALID_AMOUNT", "支付金额无效: " + amount);
    }

    /**
     * 创建余额不足异常
     * 
     * @param userId 用户ID
     * @param requiredAmount 需要金额
     * @param availableAmount 可用金额
     * @return 支付异常
     */
    public static PaymentException insufficientBalance(String userId, String requiredAmount, String availableAmount) {
        return new PaymentException("INSUFFICIENT_BALANCE", 
                                  String.format("余额不足，用户ID: %s, 需要金额: %s, 可用金额: %s", 
                                              userId, requiredAmount, availableAmount));
    }

    /**
     * 创建第三方支付异常
     * 
     * @param paymentMethod 支付方式
     * @param errorCode 第三方错误码
     * @param errorMessage 第三方错误信息
     * @return 支付异常
     */
    public static PaymentException thirdPartyPaymentError(String paymentMethod, String errorCode, String errorMessage) {
        return new PaymentException("THIRD_PARTY_PAYMENT_ERROR", 
                                  String.format("第三方支付异常，支付方式: %s, 错误码: %s, 错误信息: %s", 
                                              paymentMethod, errorCode, errorMessage));
    }

    /**
     * 创建支付超时异常
     * 
     * @param orderId 订单ID
     * @return 支付异常
     */
    public static PaymentException paymentTimeout(String orderId) {
        return new PaymentException("PAYMENT_TIMEOUT", "支付超时，订单ID: " + orderId);
    }

    /**
     * 创建重复支付异常
     * 
     * @param orderId 订单ID
     * @return 支付异常
     */
    public static PaymentException duplicatePayment(String orderId) {
        return new PaymentException("DUPLICATE_PAYMENT", "重复支付，订单ID: " + orderId);
    }

    /**
     * 创建退款金额超限异常
     * 
     * @param orderId 订单ID
     * @param refundAmount 退款金额
     * @param maxRefundAmount 最大可退款金额
     * @return 支付异常
     */
    public static PaymentException refundAmountExceeded(String orderId, String refundAmount, String maxRefundAmount) {
        return new PaymentException("REFUND_AMOUNT_EXCEEDED", 
                                  String.format("退款金额超限，订单ID: %s, 退款金额: %s, 最大可退款金额: %s", 
                                              orderId, refundAmount, maxRefundAmount));
    }

    /**
     * 创建退款不支持异常
     * 
     * @param paymentMethod 支付方式
     * @return 支付异常
     */
    public static PaymentException refundNotSupported(String paymentMethod) {
        return new PaymentException("REFUND_NOT_SUPPORTED", "支付方式不支持退款: " + paymentMethod);
    }

    /**
     * 创建签名验证失败异常
     * 
     * @param paymentMethod 支付方式
     * @return 支付异常
     */
    public static PaymentException signatureVerificationFailed(String paymentMethod) {
        return new PaymentException("SIGNATURE_VERIFICATION_FAILED", "签名验证失败，支付方式: " + paymentMethod);
    }

    /**
     * 创建配置错误异常
     * 
     * @param configKey 配置键
     * @return 支付异常
     */
    public static PaymentException configurationError(String configKey) {
        return new PaymentException("CONFIGURATION_ERROR", "配置错误: " + configKey);
    }

    /**
     * 创建网络异常
     * 
     * @param message 错误信息
     * @param cause 原因异常
     * @return 支付异常
     */
    public static PaymentException networkError(String message, Throwable cause) {
        return new PaymentException("NETWORK_ERROR", "网络异常: " + message, cause);
    }

    /**
     * 创建系统异常
     * 
     * @param message 错误信息
     * @param cause 原因异常
     * @return 支付异常
     */
    public static PaymentException systemError(String message, Throwable cause) {
        return new PaymentException("SYSTEM_ERROR", "系统异常: " + message, cause);
    }
    
    /**
     * 创建系统异常
     * 
     * @param message 错误信息
     * @return 支付异常
     */
    public static PaymentException systemError(String message) {
        return new PaymentException("SYSTEM_ERROR", "系统异常: " + message);
    }
    
    /**
     * 创建订单正在处理异常
     * 
     * @param message 错误信息
     * @return 支付异常
     */
    public static PaymentException orderProcessing(String message) {
        return new PaymentException("ORDER_PROCESSING", message);
    }
    
    /**
     * 创建重复订单异常
     * 
     * @param message 错误信息
     * @return 支付异常
     */
    public static PaymentException duplicateOrder(String message) {
        return new PaymentException("DUPLICATE_ORDER", message);
    }
    
    /**
     * 风控拒绝异常
     * 
     * @param message 错误信息
     * @return PaymentException实例
     */
    public static PaymentException riskControlReject(String message) {
        return new PaymentException("RISK_CONTROL_REJECT", message);
    }
    
    /**
     * 记录未找到异常
     * 
     * @param message 错误信息
     * @return PaymentException实例
     */
    public static PaymentException recordNotFound(String message) {
        return new PaymentException("RECORD_NOT_FOUND", message);
    }
    
    /**
     * 无效状态异常
     * 
     * @param message 错误信息
     * @return PaymentException实例
     */
    public static PaymentException invalidState(String message) {
        return new PaymentException("INVALID_STATE", message);
    }
}