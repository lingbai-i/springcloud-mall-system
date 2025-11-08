package com.mall.admin.domain.dto;

import lombok.Data;

/**
 * 用户查询请求DTO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
public class UserQueryRequest {
    
    /** 页码 */
    private Integer page = 1;
    
    /** 每页大小 */
    private Integer size = 10;
    
    /** 搜索关键词 */
    private String keyword;
    
    /** 用户状态 */
    private Integer status;
    
    /** 开始时间 */
    private String startTime;
    
    /** 结束时间 */
    private String endTime;
}