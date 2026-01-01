package com.mall.user.controller;

import com.mall.user.domain.entity.Address;
import com.mall.user.service.AddressService;
import com.mall.user.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地址管理控制器
 *
 * @author lingbai
 * @version 1.0 
 * @since 2025-01-21
 */
@Slf4j
@RestController
@RequestMapping({"/api/user-service/addresses", "/addresses"})
@Tag(name = "地址管理", description = "收货地址相关接口")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final JwtUtils jwtUtils;
    private final com.mall.user.service.UserService userService;

    /**
     * 获取当前用户的地址列表
     */
    @GetMapping
    @Operation(summary = "获取地址列表")
    public ResponseEntity<Map<String, Object>> getAddresses(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<Address> addresses = addressService.getUserAddresses(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取地址列表成功");
            response.put("data", addresses);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取地址列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取地址列表失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取地址详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取地址详情")
    public ResponseEntity<Map<String, Object>> getAddress(
            @Parameter(description = "地址ID") @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Address address = addressService.getAddressById(id, userId);

            Map<String, Object> response = new HashMap<>();
            if (address != null) {
                response.put("success", true);
                response.put("message", "获取地址详情成功");
                response.put("data", address);
            } else {
                response.put("success", false);
                response.put("message", "地址不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取地址详情失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取地址详情失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 添加地址
     */
    @PostMapping
    @Operation(summary = "添加地址")
    public ResponseEntity<Map<String, Object>> addAddress(
            @RequestBody Address address,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            address.setUserId(userId);

            Address savedAddress = addressService.addAddress(address);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "添加地址成功");
            response.put("data", savedAddress);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("添加地址失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "添加地址失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 更新地址
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新地址")
    public ResponseEntity<Map<String, Object>> updateAddress(
            @Parameter(description = "地址ID") @PathVariable Long id,
            @RequestBody Address address,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Address updatedAddress = addressService.updateAddress(id, address, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "更新地址成功");
            response.put("data", updatedAddress);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新地址失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新地址失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址")
    public ResponseEntity<Map<String, Object>> deleteAddress(
            @Parameter(description = "地址ID") @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            boolean success = addressService.deleteAddress(id, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "删除地址成功" : "删除地址失败");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除地址失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除地址失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/{id}/default")
    @Operation(summary = "设置默认地址")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(
            @Parameter(description = "地址ID") @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            boolean success = addressService.setDefaultAddress(id, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "设置默认地址成功" : "设置默认地址失败");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("设置默认地址失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "设置默认地址失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String username = null;
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                username = jwtUtils.getUsernameFromToken(token);
            } catch (Exception e) {
                log.error("从token获取用户名失败", e);
            }
        }

        // 如果无法从token获取，使用默认测试用户（开发模式）
        if (username == null) {
            username = "user_17698275192";
            log.warn("无法从token获取用户名，使用默认测试用户: {}", username);
        }

        // 通过用户名查询用户ID
        try {
            com.mall.user.domain.vo.UserInfoResponse userInfo = userService.getUserInfo(username);
            if (userInfo != null) {
                return userInfo.getUserId();
            }
        } catch (Exception e) {
            log.error("通过用户名查询用户ID失败: username={}", username, e);
        }

        // 默认返回测试用户ID
        log.warn("无法获取用户ID，使用默认测试用户ID: 19");
        return 19L;
    }
}
