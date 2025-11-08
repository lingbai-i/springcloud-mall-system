package com.mall.product.model;

import java.time.LocalDateTime;

/**
 * 价格版本实体类
 * 用于记录价格的版本变更历史
 */
public class PriceVersion {
    
    private Long id;
    private Long productId;
    private Long skuId;
    private Long version;
    private Double oldPrice;
    private Double newPrice;
    private String changeReason;
    private Long operatorId;
    private LocalDateTime createTime;
    private String status;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Long getSkuId() {
        return skuId;
    }
    
    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    public Double getOldPrice() {
        return oldPrice;
    }
    
    public void setOldPrice(Double oldPrice) {
        this.oldPrice = oldPrice;
    }
    
    public Double getNewPrice() {
        return newPrice;
    }
    
    public void setNewPrice(Double newPrice) {
        this.newPrice = newPrice;
    }
    
    public String getChangeReason() {
        return changeReason;
    }
    
    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }
    
    public Long getOperatorId() {
        return operatorId;
    }
    
    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}