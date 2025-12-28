package com.mall.product.controller;

import com.mall.common.core.domain.R;
import com.mall.common.core.minio.MinioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 * 
 * @author mall
 * @since 2025-12-28
 */
@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private MinioService minioService;

    /**
     * 上传单个图片
     * 
     * @param file 图片文件
     * @param folder 存储文件夹（默认reviews）
     * @return 图片URL
     */
    @PostMapping("/upload")
    public R<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "reviews") String folder) {
        logger.info("上传文件 - 文件名: {}, 大小: {}, 文件夹: {}", 
                file.getOriginalFilename(), file.getSize(), folder);
        
        try {
            String url = minioService.uploadFile(file, folder);
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return R.ok(result);
        } catch (Exception e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传图片
     * 
     * @param files 图片文件列表
     * @param folder 存储文件夹
     * @return 图片URL列表
     */
    @PostMapping("/upload/batch")
    public R<List<String>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", defaultValue = "reviews") String folder) {
        logger.info("批量上传文件 - 数量: {}, 文件夹: {}", files.length, folder);
        
        List<String> urls = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String url = minioService.uploadFile(file, folder);
                urls.add(url);
            } catch (Exception e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            logger.warn("部分文件上传失败: {}", errors);
        }
        
        return R.ok(urls);
    }

    /**
     * 删除文件
     * 
     * @param url 文件URL
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    public R<Boolean> deleteFile(@RequestParam("url") String url) {
        logger.info("删除文件: {}", url);
        
        try {
            minioService.deleteFile(url);
            return R.ok(true);
        } catch (Exception e) {
            logger.error("文件删除失败: {}", e.getMessage(), e);
            return R.fail("文件删除失败: " + e.getMessage());
        }
    }
}
