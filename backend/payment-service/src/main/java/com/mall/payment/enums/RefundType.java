package com.mall.payment.enums;

import lombok.Getter;

/**
 * 退款类型枚举
 * 定义不同的退款发起类型，用于区分退款的来源和处理方式
 * 
 * <p>退款类型说明：</p>
 * <ul>
 *   <li>USER_APPLIED（用户申请）：用户主动发起的退款申请，需要审核流程</li>
 *   <li>SYSTEM_AUTO（系统自动）：系统根据业务规则自动触发的退款，如订单超时、库存不足等</li>
 *   <li>CUSTOMER_SERVICE（客服处理）：客服人员根据用户投诉或问题手动处理的退款</li>
 *   <li>MERCHANT_AGREED（商家同意）：商家主动同意的退款，通常用于商家端操作</li>
 *   <li>RISK_CONTROL（风控退款）：风控系统检测到异常交易后触发的退款</li>
 * </ul>
 * 
 * <p>处理特点：</p>
 * <ul>
 *   <li>用户申请：需要完整的审核流程，可能需要提供退款凭证</li>
 *   <li>系统自动：无需审核，直接进入退款处理流程</li>
 *   <li>人工处理：客服处理和商家同意通常有特殊的审批权限</li>
 *   <li>风控退款：优先级最高，需要立即处理</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加退款类型说明和处理特点
 * V1.1 2024-12-22：新增风控退款类型
 * V1.0 2024-12-01：初始版本，定义基本退款类型
 */
@Getter
public enum RefundType {
    
    /**
     * 用户申请 - 用户主动申请的退款
     */
    USER_APPLIED(1, "用户申请"),
    
    /**
     * 系统自动 - 系统自动触发的退款（如订单超时等）
     */
    SYSTEM_AUTO(2, "系统自动"),
    
    /**
     * 客服处理 - 客服人员手动处理的退款
     */
    CUSTOMER_SERVICE(3, "客服处理"),
    
    /**
     * 商家同意 - 商家主动同意的退款
     */
    MERCHANT_AGREED(4, "商家同意"),
    
    /**
     * 风控退款 - 风控系统触发的退款
     */
    RISK_CONTROL(5, "风控退款");

    /**
     * 类型代码
     */
    private final Integer code;
    
    /**
     * 类型描述
     */
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param code 类型代码
     * @param description 类型描述
     */
    RefundType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }


    /**
     * 根据类型代码获取对应的枚举值
     * 
     * @param code 类型代码
     * @return 对应的退款类型枚举，如果未找到则返回null
     */
    public static RefundType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RefundType type : RefundType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为用户申请的退款
     * 
     * @return 如果是用户申请返回true，否则返回false
     */
    public boolean isUserApplied() {
        return this == USER_APPLIED;
    }

    /**
     * 判断是否为系统自动退款
     * 
     * @return 如果是系统自动返回true，否则返回false
     */
    public boolean isSystemAuto() {
        return this == SYSTEM_AUTO;
    }

    /**
     * 判断是否为人工处理的退款
     * 
     * @return 如果是人工处理返回true，否则返回false
     */
    public boolean isManualProcess() {
        return this == CUSTOMER_SERVICE || this == MERCHANT_AGREED;
    }

    /**
     * 判断是否为风控退款
     * 
     * @return 如果是风控退款返回true，否则返回false
     */
    public boolean isRiskControl() {
        return this == RISK_CONTROL;
    }

    public Integer getCode() {
        return code;
    }
}