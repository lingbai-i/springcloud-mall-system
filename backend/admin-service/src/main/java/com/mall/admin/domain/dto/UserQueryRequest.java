package com.mall.admin.domain.dto;

import lombok.Data;

/**
 * 用户查询请求DTO
 */
@Data
public class UserQueryRequest {
    
    /**
     * 关键词(用户名/手机/邮箱)
     */
    private String keyword;
    
    /**
     * 状态筛选
     */
    private Integer status;
    
    /**
     * 注册开始日期
     */
    private String startDate;
    
    /**
     * 注册结束日期
     */
    private String endDate;
    
    /**
     * 页码,默认1
     */
    private Integer page = 1;
    
    /**
     * 每页大小,默认10
     */
    private Integer pageSize = 10;
}
