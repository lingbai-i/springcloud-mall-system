package com.mall.payment.enums;

import lombok.Getter;

/**
 * 退款状态枚举
 * 定义退款订单的各种状态，用于跟踪退款流程的进展
 * 
 * <p>退款状态流转说明：</p>
 * <ul>
 *   <li>PENDING（待审核）→ REVIEWING（审核中）→ APPROVED（审核通过）→ PROCESSING（处理中）→ SUCCESS（退款成功）</li>
 *   <li>PENDING（待审核）→ CANCELLED（已取消）</li>
 *   <li>REVIEWING（审核中）→ REJECTED（审核拒绝）</li>
 *   <li>REVIEWING（审核中）→ PENDING_REVIEW（待人工审核）</li>
 *   <li>PROCESSING（处理中）→ FAILED（退款失败）</li>
 *   <li>PROCESSING（处理中）→ EXCEPTION（异常）</li>
 *   <li>FAILED（退款失败）→ PROCESSING（处理中）[重试]</li>
 *   <li>EXCEPTION（异常）→ PROCESSING（处理中）[人工处理后]</li>
 * </ul>
 * 
 * <p>状态分类：</p>
 * <ul>
 *   <li>进行中状态：PENDING、REVIEWING、PENDING_REVIEW、APPROVED、PROCESSING</li>
 *   <li>终态状态：SUCCESS、REJECTED、CANCELLED</li>
 *   <li>异常状态：FAILED、EXCEPTION（需要人工处理）</li>
 *   <li>可取消状态：PENDING、REVIEWING、APPROVED</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加状态流转说明和分类说明
 * V1.1 2024-12-18：新增待人工审核状态和异常状态
 * V1.0 2024-12-01：初始版本，定义基本退款状态
 */
@Getter
public enum RefundStatus {
    
    /**
     * 待审核 - 退款申请已提交，等待审核
     */
    PENDING("PENDING", "待审核"),
    
    /**
     * 审核中 - 退款申请正在审核中
     */
    REVIEWING("REVIEWING", "审核中"),
    
    /**
     * 待人工审核 - 需要人工介入审核的退款申请
     */
    PENDING_REVIEW("PENDING_REVIEW", "待人工审核"),
    
    /**
     * 审核通过 - 退款申请审核通过，准备处理
     */
    APPROVED("APPROVED", "审核通过"),
    
    /**
     * 审核拒绝 - 退款申请被拒绝
     */
    REJECTED("REJECTED", "审核拒绝"),
    
    /**
     * 处理中 - 退款正在处理中，已向第三方发起退款
     */
    PROCESSING("PROCESSING", "处理中"),
    
    /**
     * 退款成功 - 退款已成功，资金已退回
     */
    SUCCESS("SUCCESS", "退款成功"),
    
    /**
     * 退款失败 - 退款处理失败
     */
    FAILED("FAILED", "退款失败"),
    
    /**
     * 已取消 - 退款申请被取消
     */
    CANCELLED("CANCELLED", "已取消"),
    
    /**
     * 异常 - 退款过程中出现异常，需要人工处理
     */
    EXCEPTION("EXCEPTION", "异常");

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
    RefundStatus(String code, String description) {
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
     * 根据代码获取退款状态
     * 
     * @param code 状态代码
     * @return 退款状态枚举，如果未找到则返回null
     */
    public static RefundStatus fromCode(String code) {
        for (RefundStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为终态状态
     * 终态状态包括：成功、失败、已取消、审核拒绝
     * 
     * @return 如果是终态状态返回true，否则返回false
     */
    public boolean isFinalStatus() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == REJECTED;
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
     * 判断是否可以取消
     * 只有待审核、审核中、审核通过状态才可以取消
     * 
     * @return 如果可以取消返回true，否则返回false
     */
    public boolean canCancel() {
        return this == PENDING || this == REVIEWING || this == APPROVED;
    }

    /**
     * 判断是否需要人工处理
     * 
     * @return 如果需要人工处理返回true，否则返回false
     */
    public boolean needManualProcess() {
        return this == EXCEPTION || this == FAILED;
    }

    /**
     * 获取下一个可能的状态
     * 
     * @return 下一个可能的状态数组
     */
    public RefundStatus[] getNextPossibleStatuses() {
        switch (this) {
            case PENDING:
                return new RefundStatus[]{REVIEWING, CANCELLED};
            case REVIEWING:
                return new RefundStatus[]{APPROVED, REJECTED, CANCELLED};
            case APPROVED:
                return new RefundStatus[]{PROCESSING, CANCELLED};
            case PROCESSING:
                return new RefundStatus[]{SUCCESS, FAILED, EXCEPTION};
            case FAILED:
                return new RefundStatus[]{PROCESSING, EXCEPTION};
            case EXCEPTION:
                return new RefundStatus[]{PROCESSING, FAILED, CANCELLED};
            default:
                return new RefundStatus[0]; // 终态状态无后续状态
        }
    }
}