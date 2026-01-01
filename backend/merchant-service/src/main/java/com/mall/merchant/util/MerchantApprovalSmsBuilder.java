package com.mall.merchant.util;

/**
 * 商家审批短信内容构建器
 * 用于生成符合规范的审核结果通知短信内容
 *
 * @author lingbai
 * @since 2025-12-31
 */
public class MerchantApprovalSmsBuilder {

    /**
     * 平台名称
     */
    public static final String PLATFORM_NAME = "百物语";

    /**
     * 默认拒绝原因
     */
    public static final String DEFAULT_REJECTION_REASON = "资料不完整或不符合要求";

    /**
     * 构建审核通过短信内容
     * 格式：尊敬的用户，您在百物语的商家入驻申请已通过。请您使用申请时填写的账号密码进行登录。
     *
     * @return 格式化的短信内容
     */
    public static String buildApprovalMessage() {
        return String.format(
            "尊敬的用户，您在%s的商家入驻申请已通过。请您使用申请时填写的账号密码进行登录。",
            PLATFORM_NAME
        );
    }

    /**
     * 构建审核拒绝短信内容
     * 格式：尊敬的用户，您在百物语的商家入驻申请未通过，原因为{拒绝原因}。请您修改后重新提交。
     *
     * @param rejectionReason 拒绝原因
     * @return 格式化的短信内容
     */
    public static String buildRejectionMessage(String rejectionReason) {
        String reason = normalizeRejectionReason(rejectionReason);
        return String.format(
            "尊敬的用户，您在%s的商家入驻申请未通过，原因为%s。请您修改后重新提交。",
            PLATFORM_NAME,
            reason
        );
    }

    /**
     * 规范化拒绝原因
     * 如果拒绝原因为空或仅包含空白字符，则返回默认拒绝原因
     *
     * @param reason 原始拒绝原因
     * @return 规范化后的拒绝原因
     */
    public static String normalizeRejectionReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return DEFAULT_REJECTION_REASON;
        }
        return reason.trim();
    }

    /**
     * 验证短信内容是否包含平台名称
     *
     * @param message 短信内容
     * @return 是否包含平台名称
     */
    public static boolean containsPlatformName(String message) {
        return message != null && message.contains(PLATFORM_NAME);
    }

    /**
     * 验证短信内容是否以正式礼貌用语开头
     *
     * @param message 短信内容
     * @return 是否以"尊敬的用户"开头
     */
    public static boolean startsWithFormalGreeting(String message) {
        return message != null && message.startsWith("尊敬的用户");
    }
}
