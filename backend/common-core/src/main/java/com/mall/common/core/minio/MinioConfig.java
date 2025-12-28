package com.mall.common.core.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO自动配置类
 * 
 * @author mall
 * @since 2025-12-28
 */
@Slf4j
@Configuration
@ConditionalOnClass(MinioClient.class)
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    @ConditionalOnMissingBean
    public MinioClient minioClient(MinioProperties properties) {
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(properties.getEndpoint())
                    .credentials(properties.getAccessKey(), properties.getSecretKey())
                    .build();

            // 创建bucket（如果不存在）
            createBucketIfNotExists(client, properties.getBucketName());

            log.info("MinIO初始化成功 - endpoint: {}, bucket: {}", 
                    properties.getEndpoint(), properties.getBucketName());
            return client;

        } catch (Exception e) {
            log.error("MinIO初始化失败: {}", e.getMessage());
            throw new RuntimeException("MinIO初始化失败: " + e.getMessage());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public MinioService minioService(MinioClient minioClient, MinioProperties properties) {
        return new MinioService(minioClient, properties);
    }

    /**
     * 创建bucket并设置为公开读
     */
    private void createBucketIfNotExists(MinioClient client, String bucketName) throws Exception {
        boolean exists = client.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build());

        if (!exists) {
            client.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());

            // 设置为公开读
            String policy = String.format("""
                {
                    "Version": "2012-10-17",
                    "Statement": [{
                        "Effect": "Allow",
                        "Principal": {"AWS": ["*"]},
                        "Action": ["s3:GetObject"],
                        "Resource": ["arn:aws:s3:::%s/*"]
                    }]
                }
                """, bucketName);

            client.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build());

            log.info("已创建MinIO bucket: {} 并设置为公开读", bucketName);
        }
    }
}
