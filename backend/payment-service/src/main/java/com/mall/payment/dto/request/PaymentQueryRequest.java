package com.mall.payment.dto.request;

import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单查询请求DTO
 * 用于接收支付订单查询的请求参数，支持多种查询条件
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Data
public class PaymentQueryRequest {

    /**
     * 支付订单ID
     */
    @Size(max = 36, message = "支付订单ID长度不能超过36个字符")
    private String paymentId;

    /**
     * 业务订单ID
     */
    @Size(max = 64, message = "业务订单ID长度不能超过64个字符")
    private String orderId;

    /**
     * 用户ID
     */
    @Size(max = 36, message = "用户ID长度不能超过36个字符")
    private String userId;

    /**
     * 支付状态
     */
    private PaymentStatus status;

    /**
     * 支付方式
     */
    private PaymentMethod paymentMethod;

    /**
     * 第三方支付订单号
     */
    @Size(max = 64, message = "第三方支付订单号长度不能超过64个字符")
    private String thirdPartyOrderNo;

    /**
     * 创建时间开始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAtStart;

    /**
     * 创建时间结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAtEnd;

    /**
     * 支付完成时间开始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTimeStart;

    /**
     * 支付完成时间结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTimeEnd;

    /**
     * 页码 - 从1开始
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;

    /**
     * 排序字段 - 默认按创建时间排序
     */
    @Size(max = 50, message = "排序字段长度不能超过50个字符")
    private String sortBy = "createdAt";

    /**
     * 排序方向 - ASC或DESC，默认DESC
     */
    @Size(max = 4, message = "排序方向长度不能超过4个字符")
    private String sortDirection = "DESC";

    /**
     * 是否包含支付记录
     */
    private Boolean includeRecords = false;

    /**
     * 是否包含退款订单
     */
    private Boolean includeRefunds = false;

    /**
     * 最小金额
     */
    private BigDecimal minAmount;

    /**
     * 最大金额
     */
    private BigDecimal maxAmount;

    // Setter methods for fields that Lombok is not generating
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setCreatedAtStart(LocalDateTime createdAtStart) {
        this.createdAtStart = createdAtStart;
    }

    public void setCreatedAtEnd(LocalDateTime createdAtEnd) {
        this.createdAtEnd = createdAtEnd;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getSize() {
        return size;
    }

    // Getter methods for fields that Lombok is not generating
    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public LocalDateTime getCreatedAtStart() {
        return createdAtStart;
    }

    public LocalDateTime getCreatedAtEnd() {
        return createdAtEnd;
    }

    public String getUserId() {
        return userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * 获取偏移量
     * 用于数据库分页查询
     * 
     * @return 偏移量
     */
    public int getOffset() {
        return (page - 1) * size;
    }

    /**
     * 验证时间范围是否有效
     * 开始时间不能晚于结束时间
     * 
     * @return 如果时间范围有效返回true，否则返回false
     */
    public boolean isValidTimeRange() {
        if (createdAtStart != null && createdAtEnd != null) {
            return !createdAtStart.isAfter(createdAtEnd);
        }
        if (payTimeStart != null && payTimeEnd != null) {
            return !payTimeStart.isAfter(payTimeEnd);
        }
        return true;
    }

    /**
     * 验证排序参数是否有效
     * 
     * @return 如果排序参数有效返回true，否则返回false
     */
    public boolean isValidSort() {
        if (sortDirection == null) {
            return false;
        }
        return "ASC".equalsIgnoreCase(sortDirection) || "DESC".equalsIgnoreCase(sortDirection);
    }

    /**
     * 获取标准化的排序方向
     * 
     * @return 标准化的排序方向（ASC或DESC）
     */
    public String getNormalizedSortDirection() {
        if (sortDirection == null) {
            return "DESC";
        }
        return sortDirection.toUpperCase();
    }

    /**
     * 判断是否有查询条件
     * 
     * @return 如果有查询条件返回true，否则返回false
     */
    public boolean hasQueryConditions() {
        return paymentId != null || orderId != null || userId != null || 
               status != null || paymentMethod != null || thirdPartyOrderNo != null ||
               createdAtStart != null || createdAtEnd != null ||
               payTimeStart != null || payTimeEnd != null;
    }

    /**
     * 重置分页参数为默认值
     */
    public void resetPagination() {
        this.page = 1;
        this.size = 10;
    }

    /**
     * 设置默认时间范围（最近30天）
     */
    public void setDefaultTimeRange() {
        if (createdAtStart == null && createdAtEnd == null) {
            this.createdAtEnd = LocalDateTime.now();
            this.createdAtStart = this.createdAtEnd.minusDays(30);
        }
    }
}