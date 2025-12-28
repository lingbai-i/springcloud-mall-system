package com.mall.common.core.minio;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * MinIO文件存储服务
 * 提供通用的文件上传、删除功能
 * 
 * @author mall
 * @since 2025-12-28
 */
@Slf4j
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    /**
     * 上传文件
     * 
     * @param file 文件
     * @param folder 文件夹（如 avatars, reviews, products）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            validateFile(file);
            
            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = folder + "/" + UUID.randomUUID().toString() + extension;

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(properties.getBucketName())
                                .object(fileName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build());
            }

            String url = String.format("%s/%s/%s", 
                    properties.getPublicUrl(), 
                    properties.getBucketName(), 
                    fileName);
            
            log.info("文件上传成功: {}", url);
            return url;

        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传Base64编码的图片
     * 
     * @param base64Data Base64编码字符串（格式：data:image/png;base64,xxxxx）
     * @param folder 文件夹
     * @return 文件访问URL
     */
    public String uploadBase64(String base64Data, String folder) {
        try {
            if (base64Data == null || base64Data.isBlank()) {
                throw new IllegalArgumentException("Base64数据不能为空");
            }

            // 解析Base64格式
            String[] parts = base64Data.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Base64格式错误");
            }

            String header = parts[0];
            String contentType = header.substring(5, header.indexOf(";"));
            String extension = "." + contentType.substring(contentType.indexOf("/") + 1);

            byte[] imageBytes = Base64.getDecoder().decode(parts[1]);
            String fileName = folder + "/" + UUID.randomUUID().toString() + extension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(fileName)
                            .stream(new ByteArrayInputStream(imageBytes), imageBytes.length, -1)
                            .contentType(contentType)
                            .build());

            String url = String.format("%s/%s/%s",
                    properties.getPublicUrl(),
                    properties.getBucketName(),
                    fileName);

            log.info("Base64图片上传成功: {}", url);
            return url;

        } catch (Exception e) {
            log.error("Base64图片上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("Base64图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isBlank()) {
                return;
            }

            // 跳过Base64格式
            if (fileUrl.startsWith("data:image")) {
                return;
            }

            // 提取objectName
            String prefix = properties.getPublicUrl() + "/" + properties.getBucketName() + "/";
            if (!fileUrl.startsWith(prefix)) {
                log.debug("跳过删除非当前bucket的文件: {}", fileUrl);
                return;
            }

            String objectName = fileUrl.substring(prefix.length());
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .build());

            log.info("文件删除成功: {}", fileUrl);

        } catch (Exception e) {
            log.warn("文件删除失败: {}", e.getMessage());
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
            throw new IllegalArgumentException("仅支持图片格式");
        }

        // 限制文件大小为5MB
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
