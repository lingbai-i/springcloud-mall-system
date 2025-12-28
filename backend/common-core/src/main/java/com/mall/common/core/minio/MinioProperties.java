package com.mall.common.core.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO配置属性
 * 
 * @author mall
 * @since 2025-12-28
 */
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    
    /**
     * MinIO服务端点
     */
    private String endpoint = "http://localhost:9000";
    
    /**
     * 访问密钥
     */
    private String accessKey = "minioadmin";
    
    /**
     * 秘密密钥
     */
    private String secretKey = "minioadmin123";
    
    /**
     * 存储桶名称
     */
    private String bucketName = "mall-files";
    
    /**
     * 公开访问URL（用于返回给前端的图片地址）
     */
    private String publicUrl = "http://localhost:9000";
}
