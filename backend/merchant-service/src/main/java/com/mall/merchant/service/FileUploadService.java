package com.mall.merchant.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.util.UUID;

/**
 * 文件上传服务
 * 
 * @author lingbai
 * @since 2025-11-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

  private final MinioClient minioClient;

  @Value("${minio.bucket-name:mall-products}")
  private String bucketName;

  @Value("${minio.public-url:http://localhost:9000}")
  private String publicUrl;

  /**
   * 初始化方法，确保存储桶存在
   */
  @PostConstruct
  public void init() {
    try {
      // 检查存储桶是否存在
      boolean exists = minioClient.bucketExists(
          BucketExistsArgs.builder()
              .bucket(bucketName)
              .build());

      if (!exists) {
        // 创建存储桶
        minioClient.makeBucket(
            MakeBucketArgs.builder()
                .bucket(bucketName)
                .build());
        log.info("MinIO存储桶创建成功: {}", bucketName);

        // 设置存储桶为公开读权限
        String policy = String.format(
            "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}",
            bucketName);

        minioClient.setBucketPolicy(
            SetBucketPolicyArgs.builder()
                .bucket(bucketName)
                .config(policy)
                .build());
        log.info("MinIO存储桶权限设置成功: {}", bucketName);
      } else {
        log.info("MinIO存储桶已存在: {}", bucketName);
      }
    } catch (Exception e) {
      log.error("初始化MinIO存储桶失败", e);
    }
  }

  /**
   * 上传商品图片
   * 
   * @param file 图片文件
   * @return 图片URL
   */
  public String uploadProductImage(MultipartFile file) {
    try {
      // 验证文件
      validateFile(file);

      // 生成唯一文件名
      String fileName = generateFileName(file.getOriginalFilename());

      // 上传到MinIO
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucketName)
              .object(fileName)
              .stream(file.getInputStream(), file.getSize(), -1)
              .contentType(file.getContentType())
              .build());

      // 返回完整URL
      String imageUrl = String.format("%s/%s/%s", publicUrl, bucketName, fileName);

      log.info("商品图片上传成功: {}", fileName);
      return imageUrl;

    } catch (Exception e) {
      log.error("商品图片上传失败", e);
      throw new RuntimeException("图片上传失败: " + e.getMessage());
    }
  }

  /**
   * 验证文件
   */
  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("文件不能为空");
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new IllegalArgumentException("只支持上传图片文件");
    }

    // 限制文件大小为5MB
    if (file.getSize() > 5 * 1024 * 1024) {
      throw new IllegalArgumentException("图片大小不能超过5MB");
    }
  }

  /**
   * 生成文件名
   */
  private String generateFileName(String originalFilename) {
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    return String.format("product_%d_%s%s",
        System.currentTimeMillis(),
        UUID.randomUUID().toString().substring(0, 8),
        extension);
  }
}
