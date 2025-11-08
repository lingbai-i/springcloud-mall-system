package com.mall.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 用户统计数据DTO
 * 用于封装用户相关的统计信息
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {

    /**
     * 总用户数（有支付记录的用户）
     */
    private Long totalUsers;

    /**
     * 活跃用户数（成功支付的用户）
     */
    private Long activeUsers;

    /**
     * 新用户数（首次支付的用户）
     */
    private Long newUsers;

    /**
     * 平均订单金额
     */
    private BigDecimal avgOrderAmount;

    /**
     * 平均每用户订单数
     */
    private Long avgOrdersPerUser;

    /**
     * 用户活跃率
     */
    private BigDecimal userActiveRate;

    /**
     * 新用户占比
     */
    private BigDecimal newUserRate;

    /**
     * 用户留存率
     */
    private BigDecimal userRetentionRate;

    /**
     * 获取用户活跃率
     * 
     * @return 用户活跃率（百分比）
     */
    public BigDecimal getUserActiveRate() {
        if (totalUsers == null || totalUsers == 0 || activeUsers == null) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(activeUsers)
                .divide(BigDecimal.valueOf(totalUsers), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 获取新用户占比
     * 
     * @return 新用户占比（百分比）
     */
    public BigDecimal getNewUserRate() {
        if (totalUsers == null || totalUsers == 0 || newUsers == null) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(newUsers)
                .divide(BigDecimal.valueOf(totalUsers), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 获取格式化的平均订单金额
     * 
     * @return 格式化的平均订单金额字符串
     */
    public String getFormattedAvgOrderAmount() {
        if (avgOrderAmount == null) {
            return "0.00元";
        }
        return String.format("%.2f元", avgOrderAmount);
    }

    /**
     * 获取格式化的用户活跃率
     * 
     * @return 格式化的用户活跃率字符串
     */
    public String getFormattedUserActiveRate() {
        BigDecimal rate = getUserActiveRate();
        return String.format("%.2f%%", rate);
    }

    /**
     * 获取格式化的新用户占比
     * 
     * @return 格式化的新用户占比字符串
     */
    public String getFormattedNewUserRate() {
        BigDecimal rate = getNewUserRate();
        return String.format("%.2f%%", rate);
    }

    /**
     * 获取格式化的用户留存率
     * 
     * @return 格式化的用户留存率字符串
     */
    public String getFormattedUserRetentionRate() {
        if (userRetentionRate == null) {
            return "0.00%";
        }
        return String.format("%.2f%%", userRetentionRate);
    }
}