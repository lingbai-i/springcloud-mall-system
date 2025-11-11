package com.mall.user.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 对象存储配置
 */
@Slf4j
@Configuration
public class MinioConfig {

  @Value("${minio.endpoint}")
  private String endpoint;

  @Value("${minio.access-key}")
  private String accessKey;

  @Value("${minio.secret-key}")
  private String secretKey;

  @Value("${minio.bucket-name}")
  private String bucketName;

  @Bean
  public MinioClient minioClient() {
    try {
      MinioClient client = MinioClient.builder()
          .endpoint(endpoint)
          .credentials(accessKey, secretKey)
          .build();

      // 创建 bucket（如果不存在）
      createBucketIfNotExists(client);

      log.info("MinIO 初始化成功 - endpoint: {}, bucket: {}", endpoint, bucketName);
      return client;

    } catch (Exception e) {
      log.error("MinIO 初始化失败", e);
      throw new RuntimeException("MinIO 初始化失败: " + e.getMessage());
    }
  }

  /**
   * 创建 bucket 并设置为公开读
   */
  private void createBucketIfNotExists(MinioClient client) throws Exception {
    boolean exists = client.bucketExists(
        BucketExistsArgs.builder()
            .bucket(bucketName)
            .build());

    if (!exists) {
      // 创建 bucket
      client.makeBucket(
          MakeBucketArgs.builder()
              .bucket(bucketName)
              .build());

      // 设置为公开读（头像图片需要外部访问）
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

      log.info("已创建 MinIO bucket: {} 并设置为公开读", bucketName);
    } else {
      log.info("MinIO bucket 已存在: {}", bucketName);
    }
  }
}
