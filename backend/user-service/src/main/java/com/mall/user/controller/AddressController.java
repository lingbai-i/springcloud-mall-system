package com.mall.user.controller;

import com.mall.common.core.domain.R;
import com.mall.user.domain.entity.Address;
import com.mall.user.service.AddressService;
import com.mall.user.service.UserService;
import com.mall.user.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址管理Controller
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Slf4j
@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {
    
    private final AddressService addressService;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    
    @Value("${security.jwt.enabled:false}")
    private boolean jwtEnabled;
    
    /**
     * 获取当前用户的地址列表
     */
    @GetMapping
    public R<List<Address>> getAddressList(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("获取用户地址列表, userId: {}", userId);
        
        List<Address> addresses = addressService.getUserAddresses(userId);
        return R.ok(addresses);
    }
    
    /**
     * 获取地址详情
     */
    @GetMapping("/{id}")
    public R<Address> getAddressDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("获取地址详情, id: {}, userId: {}", id, userId);
        
        Address address = addressService.getAddressById(id, userId);
        if (address == null) {
            return R.fail("地址不存在");
        }
        return R.ok(address);
    }
    
    /**
     * 添加地址
     */
    @PostMapping
    public R<Address> addAddress(@RequestBody Address address, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("添加地址, userId: {}, address: {}", userId, address);
        
        address.setUserId(userId);
        Address savedAddress = addressService.addAddress(address);
        return R.ok(savedAddress);
    }
    
    /**
     * 更新地址
     */
    @PutMapping("/{id}")
    public R<Address> updateAddress(@PathVariable Long id, @RequestBody Address address, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("更新地址, id: {}, userId: {}, address: {}", id, userId, address);
        
        Address updatedAddress = addressService.updateAddress(id, address, userId);
        return R.ok(updatedAddress);
    }
    
    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("删除地址, id: {}, userId: {}", id, userId);
        
        boolean success = addressService.deleteAddress(id, userId);
        return success ? R.ok() : R.fail("删除失败");
    }
    
    /**
     * 设置默认地址
     */
    @PutMapping("/{id}/default")
    public R<Void> setDefaultAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("设置默认地址, id: {}, userId: {}", id, userId);
        
        boolean success = addressService.setDefaultAddress(id, userId);
        return success ? R.ok() : R.fail("设置失败");
    }
    
    /**
     * 从请求中获取用户ID
     * 兼容开发模式和生产模式
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // 开发模式:直接返回测试用户ID
        if (!jwtEnabled) {
            log.debug("[开发模式] 使用测试用户ID: 15");
            return 15L;
        }
        
        // 生产模式:从JWT Token中获取用户ID
        String authHeader = request.getHeader("Authorization");
        log.debug("Authorization头: {}", authHeader != null ? "存在" : "不存在");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("[JWT认证] Authorization头缺失或格式错误");
            throw new RuntimeException("请先登录");
        }
        
        String token = authHeader.substring(7);
        log.debug("[JWT认证] Token长度: {}", token.length());
        
        try {
            // 验证Token
            if (!jwtUtils.validateToken(token)) {
                log.error("[JWT认证] Token验证失败");
                throw new RuntimeException("Token无效或已过期");
            }
            
            // 从Token中获取username
            String username = jwtUtils.getUsernameFromToken(token);
            log.info("[JWT认证] 从Token解析的username: {}", username);
            
            // 通过username查询用户
            com.mall.user.domain.entity.User user = userService.findByUsername(username);
            if (user == null) {
                log.error("[JWT认证] 用户不存在: username={}", username);
                throw new RuntimeException("用户不存在: " + username);
            }
            
            log.info("[JWT认证] 查询到用户: userId={}, username={}", user.getId(), user.getUsername());
            return user.getId();
            
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("[JWT认证] Token解析失败", e);
            throw new RuntimeException("Token解析失败: " + e.getMessage());
        }
    }
}
