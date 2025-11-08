package com.mall.product.controller;

import com.mall.common.core.domain.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 * 处理根路径访问，提供服务基本信息
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-10-22
 */
@RestController
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    /**
     * 根路径访问
     * 当用户直接访问服务根路径时，返回服务欢迎信息
     * 
     * @return 服务欢迎信息
     */
    @GetMapping("/")
    public R<Map<String, Object>> index() {
        logger.info("访问服务根路径");
        
        Map<String, Object> welcomeInfo = new HashMap<>();
        welcomeInfo.put("service", "商品管理微服务");
        welcomeInfo.put("version", "1.0.0");
        welcomeInfo.put("status", "运行中");
        welcomeInfo.put("timestamp", LocalDateTime.now());
        welcomeInfo.put("message", "欢迎使用商品管理微服务！");
        
        // 提供可用的API接口信息
        Map<String, String> availableApis = new HashMap<>();
        availableApis.put("健康检查", "/api/test/health");
        availableApis.put("商品查询", "/api/test/products");
        availableApis.put("商品搜索", "/api/test/search?keyword=手机");
        availableApis.put("热销商品", "/api/test/hot");
        availableApis.put("推荐商品", "/api/test/recommend");
        availableApis.put("服务信息", "/api/test/info");
        availableApis.put("库存更新", "POST /api/test/stock");
        
        welcomeInfo.put("availableApis", availableApis);
        
        logger.info("返回服务欢迎信息");
        return R.ok(welcomeInfo);
    }

    /**
     * 健康检查快捷访问
     * 提供简单的健康检查接口
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public R<String> health() {
        logger.info("快捷健康检查");
        return R.ok("服务运行正常");
    }
}