package com.mall.merchant.domain.entity;

import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商家统计数据实体类
 * 存储商家的各项统计数据
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "merchant_statistics")
public class MerchantStatistics extends BaseEntity {
    
    /**
     * 商家ID
     */
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;
    
    /**
     * 统计日期
     */
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;
    
    /**
     * 统计类型：1-日统计，2-月统计，3-年统计
     */
    @Column(name = "stat_type", nullable = false)
    private Integer statType;
    
    /**
     * 订单总数
     */
    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders = 0;
    
    /**
     * 已完成订单数
     */
    @Column(name = "completed_orders", nullable = false)
    private Integer completedOrders = 0;
    
    /**
     * 已取消订单数
     */
    @Column(name = "cancelled_orders", nullable = false)
    private Integer cancelledOrders = 0;
    
    /**
     * 退款订单数
     */
    @Column(name = "refund_orders", nullable = false)
    private Integer refundOrders = 0;
    
    /**
     * 总销售额
     */
    @Column(name = "total_sales", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSales = BigDecimal.ZERO;
    
    /**
     * 实际收入（扣除退款）
     */
    @Column(name = "actual_income", nullable = false, precision = 12, scale = 2)
    private BigDecimal actualIncome = BigDecimal.ZERO;
    
    /**
     * 退款金额
     */
    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;
    
    /**
     * 商品销售数量
     */
    @Column(name = "product_sales_count", nullable = false)
    private Integer productSalesCount = 0;
    
    /**
     * 新增商品数
     */
    @Column(name = "new_products", nullable = false)
    private Integer newProducts = 0;
    
    /**
     * 上架商品数
     */
    @Column(name = "online_products", nullable = false)
    private Integer onlineProducts = 0;
    
    /**
     * 下架商品数
     */
    @Column(name = "offline_products", nullable = false)
    private Integer offlineProducts = 0;
    
    /**
     * 访问量（PV）
     */
    @Column(name = "page_views", nullable = false)
    private Integer pageViews = 0;
    
    /**
     * 独立访客数（UV）
     */
    @Column(name = "unique_visitors", nullable = false)
    private Integer uniqueVisitors = 0;
    
    /**
     * 商品浏览量
     */
    @Column(name = "product_views", nullable = false)
    private Integer productViews = 0;
    
    /**
     * 商品收藏数
     */
    @Column(name = "product_favorites", nullable = false)
    private Integer productFavorites = 0;
    
    /**
     * 购物车添加数
     */
    @Column(name = "cart_additions", nullable = false)
    private Integer cartAdditions = 0;
    
    /**
     * 转化率（订单数/访问量）
     */
    @Column(name = "conversion_rate", precision = 5, scale = 4)
    private BigDecimal conversionRate = BigDecimal.ZERO;
    
    /**
     * 客单价（销售额/订单数）
     */
    @Column(name = "avg_order_value", precision = 10, scale = 2)
    private BigDecimal avgOrderValue = BigDecimal.ZERO;
    
    /**
     * 退款率（退款订单数/总订单数）
     */
    @Column(name = "refund_rate", precision = 5, scale = 4)
    private BigDecimal refundRate = BigDecimal.ZERO;
    
    /**
     * 好评率
     */
    @Column(name = "positive_rate", precision = 5, scale = 4)
    private BigDecimal positiveRate = BigDecimal.ZERO;
    
    /**
     * 平均评分
     */
    @Column(name = "avg_rating", precision = 3, scale = 2)
    private BigDecimal avgRating = BigDecimal.ZERO;
    
    /**
     * 评价总数
     */
    @Column(name = "total_reviews", nullable = false)
    private Integer totalReviews = 0;
    
    /**
     * 好评数
     */
    @Column(name = "positive_reviews", nullable = false)
    private Integer positiveReviews = 0;
    
    /**
     * 中评数
     */
    @Column(name = "neutral_reviews", nullable = false)
    private Integer neutralReviews = 0;
    
    /**
     * 差评数
     */
    @Column(name = "negative_reviews", nullable = false)
    private Integer negativeReviews = 0;
    
    // 业务方法
    
    /**
     * 获取统计类型文本
     * 
     * @return 统计类型文本
     */
    public String getStatTypeText() {
        switch (statType) {
            case 1:
                return "日统计";
            case 2:
                return "月统计";
            case 3:
                return "年统计";
            default:
                return "未知";
        }
    }
    
    /**
     * 计算订单完成率
     * 
     * @return 订单完成率
     */
    public BigDecimal getCompletionRate() {
        if (totalOrders == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(completedOrders)
                .divide(BigDecimal.valueOf(totalOrders), 4, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 计算订单取消率
     * 
     * @return 订单取消率
     */
    public BigDecimal getCancellationRate() {
        if (totalOrders == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(cancelledOrders)
                .divide(BigDecimal.valueOf(totalOrders), 4, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 更新转化率
     */
    public void updateConversionRate() {
        if (pageViews == 0) {
            this.conversionRate = BigDecimal.ZERO;
        } else {
            this.conversionRate = BigDecimal.valueOf(totalOrders)
                    .divide(BigDecimal.valueOf(pageViews), 4, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    /**
     * 更新客单价
     */
    public void updateAvgOrderValue() {
        if (completedOrders == 0) {
            this.avgOrderValue = BigDecimal.ZERO;
        } else {
            this.avgOrderValue = actualIncome
                    .divide(BigDecimal.valueOf(completedOrders), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    /**
     * 更新退款率
     */
    public void updateRefundRate() {
        if (totalOrders == 0) {
            this.refundRate = BigDecimal.ZERO;
        } else {
            this.refundRate = BigDecimal.valueOf(refundOrders)
                    .divide(BigDecimal.valueOf(totalOrders), 4, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    /**
     * 更新好评率
     */
    public void updatePositiveRate() {
        if (totalReviews == 0) {
            this.positiveRate = BigDecimal.ZERO;
        } else {
            this.positiveRate = BigDecimal.valueOf(positiveReviews)
                    .divide(BigDecimal.valueOf(totalReviews), 4, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    /**
     * 更新平均评分
     */
    public void updateAvgRating() {
        if (totalReviews == 0) {
            this.avgRating = BigDecimal.ZERO;
        } else {
            // 假设好评5分，中评3分，差评1分
            BigDecimal totalScore = BigDecimal.valueOf(positiveReviews * 5 + neutralReviews * 3 + negativeReviews * 1);
            this.avgRating = totalScore.divide(BigDecimal.valueOf(totalReviews), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    /**
     * 更新所有计算字段
     */
    public void updateCalculatedFields() {
        updateConversionRate();
        updateAvgOrderValue();
        updateRefundRate();
        updatePositiveRate();
        updateAvgRating();
    }
    
    /**
     * 获取订单总数
     * @return 订单总数
     */
    public Integer getTotalOrders() {
        return totalOrders;
    }
    
    /**
     * 获取总销售额
     * @return 总销售额
     */
    public BigDecimal getTotalSales() {
        return totalSales;
    }
    
    /**
     * 获取独立访客数
     * @return 独立访客数
     */
    public Integer getUniqueVisitors() {
        return uniqueVisitors;
    }
    
    /**
     * 获取页面浏览量
     * @return 页面浏览量
     */
    public Integer getPageViews() {
        return pageViews;
    }
    
    /**
     * 设置商家ID
     * @param merchantId 商家ID
     */
    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
    
    /**
     * 设置统计日期
     * @param statDate 统计日期
     */
    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }
    
    /**
     * 设置统计类型
     * @param statType 统计类型
     */
    public void setStatType(Integer statType) {
        this.statType = statType;
    }
    
    /**
     * 获取平均订单价值
     * @return 平均订单价值
     */
    public BigDecimal getAvgOrderValue() {
        return avgOrderValue;
    }
    
    /**
     * 获取转化率
     * @return 转化率
     */
    public BigDecimal getConversionRate() {
        return conversionRate;
    }

    /**
     * 获取退款率
     * @return 退款率
     */
    public BigDecimal getRefundRate() {
        return refundRate;
    }

    /**
     * 获取好评率
     * @return 好评率
     */
    public BigDecimal getPositiveRate() {
        return positiveRate;
    }

    /**
     * 获取平均评分
     * @return 平均评分
     */
    public BigDecimal getAvgRating() {
        return avgRating;
    }

    /**
     * 获取已完成订单数
     * @return 已完成订单数
     */
    public Integer getCompletedOrders() {
        return completedOrders;
    }

    /**
     * 获取已取消订单数
     * @return 已取消订单数
     */
    public Integer getCancelledOrders() {
        return cancelledOrders;
    }
    
    /**
     * 获取统计日期
     * @return 统计日期
     */
    public LocalDate getStatDate() {
        return statDate;
    }
}