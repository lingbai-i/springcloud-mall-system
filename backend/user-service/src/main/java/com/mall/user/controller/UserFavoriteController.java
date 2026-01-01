package com.mall.user.controller;

import com.mall.common.core.domain.R;
import com.mall.user.service.UserFavoriteService;
import com.mall.user.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户收藏控制器
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-10
 */
@RestController
@RequestMapping("/favorites")
@Tag(name = "用户收藏", description = "用户收藏相关接口")
public class UserFavoriteController {

    private static final Logger logger = LoggerFactory.getLogger(UserFavoriteController.class);

    @Autowired
    private UserFavoriteService userFavoriteService;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${security.jwt.enabled:true}")
    private boolean jwtEnabled;

    /**
     * 添加收藏
     */
    @PostMapping("/{productId}")
    @Operation(summary = "添加收藏", description = "将商品添加到收藏列表")
    public ResponseEntity<Map<String, Object>> addFavorite(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            HttpServletRequest request) {
        logger.info("添加收藏请求 - 商品ID: {}", productId);

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                response.put("code", 401);
                return ResponseEntity.status(401).body(response);
            }

            R<Void> result = userFavoriteService.addFavorite(userId, productId);
            
            if (result.getCode() == 200) {
                response.put("success", true);
                response.put("message", "收藏成功");
                response.put("code", 200);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMessage());
                response.put("code", 400);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("添加收藏失败", e);
            response.put("success", false);
            response.put("message", "添加收藏失败");
            response.put("code", 500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "取消收藏", description = "将商品从收藏列表移除")
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            HttpServletRequest request) {
        logger.info("取消收藏请求 - 商品ID: {}", productId);

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                response.put("code", 401);
                return ResponseEntity.status(401).body(response);
            }

            R<Void> result = userFavoriteService.removeFavorite(userId, productId);
            
            if (result.getCode() == 200) {
                response.put("success", true);
                response.put("message", "取消收藏成功");
                response.put("code", 200);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMessage());
                response.put("code", 400);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("取消收藏失败", e);
            response.put("success", false);
            response.put("message", "取消收藏失败");
            response.put("code", 500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 批量取消收藏
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量取消收藏", description = "批量将商品从收藏列表移除")
    public ResponseEntity<Map<String, Object>> batchRemoveFavorites(
            @RequestBody List<Long> productIds,
            HttpServletRequest request) {
        logger.info("批量取消收藏请求 - 商品数量: {}", productIds.size());

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                response.put("code", 401);
                return ResponseEntity.status(401).body(response);
            }

            R<Void> result = userFavoriteService.batchRemoveFavorites(userId, productIds);
            
            if (result.getCode() == 200) {
                response.put("success", true);
                response.put("message", "批量取消收藏成功");
                response.put("code", 200);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMessage());
                response.put("code", 400);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("批量取消收藏失败", e);
            response.put("success", false);
            response.put("message", "批量取消收藏失败");
            response.put("code", 500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("")
    @Operation(summary = "获取收藏列表", description = "获取当前用户的收藏列表")
    public ResponseEntity<Map<String, Object>> getFavorites(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        logger.info("获取收藏列表请求 - 页码: {}, 每页大小: {}", page, size);

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                response.put("code", 401);
                return ResponseEntity.status(401).body(response);
            }

            R<Map<String, Object>> result = userFavoriteService.getFavorites(userId, page, size);
            
            if (result.getCode() == 200) {
                response.put("success", true);
                response.put("message", "获取成功");
                response.put("code", 200);
                response.put("data", result.getData());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMessage());
                response.put("code", 400);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("获取收藏列表失败", e);
            response.put("success", false);
            response.put("message", "获取收藏列表失败");
            response.put("code", 500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 检查收藏状态
     */
    @GetMapping("/{productId}/status")
    @Operation(summary = "检查收藏状态", description = "检查商品是否已被收藏")
    public ResponseEntity<Map<String, Object>> checkFavoriteStatus(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            HttpServletRequest request) {
        logger.debug("检查收藏状态请求 - 商品ID: {}", productId);

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                // 未登录用户返回未收藏状态
                response.put("success", true);
                response.put("code", 200);
                response.put("data", false);
                return ResponseEntity.ok(response);
            }

            R<Boolean> result = userFavoriteService.isFavorited(userId, productId);
            
            if (result.getCode() == 200) {
                response.put("success", true);
                response.put("code", 200);
                response.put("data", result.getData());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMessage());
                response.put("code", 400);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("检查收藏状态失败", e);
            response.put("success", false);
            response.put("message", "检查收藏状态失败");
            response.put("code", 500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取收藏数量
     */
    @GetMapping("/count")
    @Operation(summary = "获取收藏数量", description = "获取当前用户的收藏数量")
    public ResponseEntity<Map<String, Object>> getFavoriteCount(HttpServletRequest request) {
        logger.debug("获取收藏数量请求");

        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                response.put("success", true);
                response.put("code", 200);
                response.put("data", 0);
                return ResponseEntity.ok(response);
            }

            R<Long> result = userFavoriteService.getFavoriteCount(userId);
            
            if (result.getCode() == 200) {
                response.put("success", true);
                response.put("code", 200);
                response.put("data", result.getData());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMessage());
                response.put("code", 400);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("获取收藏数量失败", e);
            response.put("success", false);
            response.put("message", "获取收藏数量失败");
            response.put("code", 500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        // 开发模式：使用默认测试用户
        if (!jwtEnabled) {
            return 1L; // 默认测试用户ID
        }

        // 生产模式：从 token 中获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtUtils.getUsernameFromToken(token);
            if (username == null || !jwtUtils.validateToken(token)) {
                return null;
            }
            // 从token中获取用户ID
            Long userId = jwtUtils.getUserIdFromToken(token);
            return userId;
        } catch (Exception e) {
            logger.error("解析token失败", e);
            return null;
        }
    }
}
