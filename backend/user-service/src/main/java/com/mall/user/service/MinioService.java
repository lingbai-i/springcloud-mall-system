package com.mall.user.service;

import cn.hutool.core.util.StrUtil;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Base64;

/**
 * MinIO 文件存储服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

  private final MinioClient minioClient;

  @Value("${minio.bucket-name}")
  private String bucketName;

  @Value("${minio.public-url}")
  private String publicUrl;

  /**
   * 上传头像文件
   * 文件名格式: avatar_{userId}_{timestamp}.{ext}
   * 
   * @param file   上传的文件
   * @param userId 用户ID
   * @return 头像访问 URL
   */
  public String uploadAvatar(MultipartFile file, Long userId) {
    try {
      // 验证文件
      validateFile(file);

      // 生成文件名：avatar_{userId}_{timestamp}.{ext}
      String fileName = generateAvatarFileName(userId, file.getOriginalFilename());

      // 上传到 MinIO
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucketName)
              .object(fileName)
              .stream(file.getInputStream(), file.getSize(), -1)
              .contentType(file.getContentType())
              .build());

      // 返回完整 URL
      String avatarUrl = String.format("%s/%s/%s", publicUrl, bucketName, fileName);

      log.info("用户 {} 头像上传成功: {}", userId, fileName);
      return avatarUrl;

    } catch (Exception e) {
      log.error("用户 {} 头像上传失败", userId, e);
      throw new RuntimeException("头像上传失败: " + e.getMessage());
    }
  }

  /**
   * 上传 Base64 编码的头像（用于数据迁移）
   * 
   * @param base64Data Base64 编码字符串
   * @param userId     用户ID
   * @return 头像访问 URL
   */
  public String uploadBase64Avatar(String base64Data, Long userId) {
    try {
      if (StrUtil.isBlank(base64Data)) {
        throw new IllegalArgumentException("Base64 数据不能为空");
      }

      // 解析 Base64 格式: data:image/png;base64,iVBORw0KG...
      String[] parts = base64Data.split(",");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Base64 格式错误");
      }

      // 提取文件类型
      String header = parts[0]; // data:image/png;base64
      String contentType = header.substring(5, header.indexOf(";"));
      String extension = contentType.substring(contentType.indexOf("/") + 1);

      // 解码 Base64
      byte[] imageBytes = Base64.getDecoder().decode(parts[1]);

      // 生成文件名
      String fileName = String.format("avatar_%d_%d.%s", userId, System.currentTimeMillis(), extension);

      // 上传到 MinIO
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucketName)
              .object(fileName)
              .stream(new ByteArrayInputStream(imageBytes), imageBytes.length, -1)
              .contentType(contentType)
              .build());

      String avatarUrl = String.format("%s/%s/%s", publicUrl, bucketName, fileName);
      log.info("用户 {} Base64 头像迁移成功: {}", userId, fileName);

      return avatarUrl;

    } catch (Exception e) {
      log.error("用户 {} Base64 头像上传失败", userId, e);
      throw new RuntimeException("Base64 头像上传失败: " + e.getMessage());
    }
  }

  /**
   * 删除旧头像
   * 
   * @param avatarUrl 头像 URL
   */
  public void deleteAvatar(String avatarUrl) {
    try {
      if (StrUtil.isBlank(avatarUrl)) {
        return;
      }

      // 跳过 Base64 格式的头像
      if (avatarUrl.startsWith("data:image")) {
        log.debug("跳过删除 Base64 格式头像");
        return;
      }

      // 跳过不属于当前 bucket 的文件
      if (!avatarUrl.contains(bucketName)) {
        log.debug("跳过删除非当前 bucket 的文件: {}", avatarUrl);
        return;
      }

      // 提取文件名
      String fileName = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);

      minioClient.removeObject(
          RemoveObjectArgs.builder()
              .bucket(bucketName)
              .object(fileName)
              .build());

      log.info("已删除旧头像: {}", fileName);

    } catch (Exception e) {
      log.warn("删除旧头像失败: {}", e.getMessage());
      // 不抛出异常，避免影响主流程
    }
  }

  /**
   * 验证上传文件
   */
  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("文件不能为空");
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new IllegalArgumentException("仅支持图片格式");
    }

    // 限制文件大小为 2MB
    long maxSize = 2 * 1024 * 1024;
    if (file.getSize() > maxSize) {
      throw new IllegalArgumentException("文件大小不能超过 2MB");
    }
  }

  /**
   * 生成头像文件名
   * 格式: avatar_{userId}_{timestamp}.{ext}
   */
  private String generateAvatarFileName(Long userId, String originalFilename) {
    String extension = getFileExtension(originalFilename);
    long timestamp = System.currentTimeMillis();
    return String.format("avatar_%d_%d%s", userId, timestamp, extension);
  }

  /**
   * 获取文件扩展名
   */
  private String getFileExtension(String filename) {
    if (StrUtil.isBlank(filename) || !filename.contains(".")) {
      return ".jpg"; // 默认扩展名
    }
    return filename.substring(filename.lastIndexOf("."));
  }
}
