package com.mall.payment.enums;

import lombok.Getter;

/**
 * 支付状态枚举
 * 定义支付订单的各种状态，用于跟踪支付流程的进展
 * 
 * <p>支付状态流转说明：</p>
 * <ul>
 *   <li>PENDING（待支付）→ PROCESSING（支付中）→ SUCCESS（支付成功）</li>
 *   <li>PENDING（待支付）→ CANCELLED（已取消）</li>
 *   <li>PENDING（待支付）→ EXPIRED（已过期）</li>
 *   <li>PROCESSING（支付中）→ FAILED（支付失败）</li>
 *   <li>SUCCESS（支付成功）→ REFUNDED（已退款）</li>
 *   <li>SUCCESS（支付成功）→ PARTIAL_REFUNDED（部分退款）</li>
 * </ul>
 * 
 * <p>状态分类：</p>
 * <ul>
 *   <li>进行中状态：PENDING、PROCESSING</li>
 *   <li>终态状态：SUCCESS、FAILED、CANCELLED、EXPIRED、REFUNDED、PARTIAL_REFUNDED</li>
 *   <li>可退款状态：SUCCESS、PARTIAL_REFUNDED</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加状态流转说明和分类说明
 * V1.1 2024-12-15：新增部分退款状态
 * V1.0 2024-12-01：初始版本，定义基本支付状态
 */
@Getter
public enum PaymentStatus {
    
    /**
     * 待支付 - 支付订单已创建，等待用户支付
     */
    PENDING("PENDING", "待支付"),
    
    /**
     * 支付中 - 用户已发起支付，正在处理中
     */
    PROCESSING("PROCESSING", "支付中"),
    
    /**
     * 支付成功 - 支付已完成，资金已到账
     */
    SUCCESS("SUCCESS", "支付成功"),
    
    /**
     * 支付失败 - 支付处理失败，需要重新支付或处理
     */
    FAILED("FAILED", "支付失败"),
    
    /**
     * 已取消 - 支付订单被用户或系统取消
     */
    CANCELLED("CANCELLED", "已取消"),
    
    /**
     * 已过期 - 支付订单超过有效期，自动失效
     */
    EXPIRED("EXPIRED", "已过期"),
    
    /**
     * 已退款 - 支付成功后发生退款
     */
    REFUNDED("REFUNDED", "已退款"),
    
    /**
     * 部分退款 - 支付成功后发生部分退款
     */
    PARTIAL_REFUNDED("PARTIAL_REFUNDED", "部分退款");

    /**
     * 状态代码
     */
    private final String code;
    
    /**
     * 状态描述
     */
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param code 状态代码
     * @param description 状态描述
     */
    PaymentStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }


    /**
     * 获取状态代码
     * 
     * @return 状态代码
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * 根据状态代码获取对应的枚举值
     * 
     * @param code 状态代码
     * @return 对应的支付状态枚举，如果未找到则返回null
     */
    public static PaymentStatus fromCode(String code) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为终态状态
     * 终态状态包括：成功、失败、已取消、已过期、已退款
     * 
     * @return 如果是终态状态返回true，否则返回false
     */
    public boolean isFinalStatus() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || 
               this == EXPIRED || this == REFUNDED || this == PARTIAL_REFUNDED;
    }

    /**
     * 判断是否为成功状态
     * 
     * @return 如果是成功状态返回true，否则返回false
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 判断是否可以退款
     * 只有支付成功或部分退款状态才可以退款
     * 
     * @return 如果可以退款返回true，否则返回false
     */
    public boolean canRefund() {
        return this == SUCCESS || this == PARTIAL_REFUNDED;
    }
}