package com.mall.admin.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 商家查询请求DTO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
public class MerchantQueryRequest {
    
    /** 页码 */
    private Integer page = 1;
    
    /** 每页大小 */
    private Integer size = 10;
    
    /** 搜索关键词 */
    private String keyword;
    
    /** 商家状态 */
    private Integer status;
    
    /** 审核状态 */
    private String auditStatus;
    
    /** 商家类别 */
    private String category;
    
    /** 日期范围 */
    private List<String> dateRange;
    
    /** 开始时间 */
    private String startTime;
    
    /** 结束时间 */
    private String endTime;
}