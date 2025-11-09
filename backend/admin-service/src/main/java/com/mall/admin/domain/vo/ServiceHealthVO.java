package com.mall.admin.domain.vo;

import lombok.Data;

/**
 * 服务健康信息VO
 */
@Data
public class ServiceHealthVO {
    
    /**
     * 服务名称
     */
    private String name;
    
    /**
     * 状态(UP/DOWN)
     */
    private String status;
    
    /**
     * 响应时间(ms)
     */
    private Long responseTime;
}
