package com.mall.admin.service.impl;

import com.mall.admin.domain.dto.AdminLoginRequest;
import com.mall.admin.domain.entity.Admin;
import com.mall.admin.domain.vo.AdminInfoResponse;
import com.mall.admin.service.AdminService;
import com.mall.admin.util.JwtUtil;
import com.mall.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员服务实现类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    // 模拟数据存储，实际项目中应该使用数据库
    private final List<Admin> adminList = new ArrayList<>();
    
    public AdminServiceImpl() {
        // 初始化默认管理员数据
        initDefaultAdmins();
    }
    
    /**
     * 初始化默认管理员数据
     */
    private void initDefaultAdmins() {
        Admin superAdmin = new Admin();
        superAdmin.setId(1L);
        superAdmin.setUsername("admin");
        superAdmin.setPassword(passwordEncoder.encode("123456"));
        superAdmin.setRealName("超级管理员");
        superAdmin.setEmail("admin@mall.com");
        superAdmin.setPhone("13800138000");
        superAdmin.setRole("super_admin");
        superAdmin.setStatus(1);
        superAdmin.setLoginCount(0);
        superAdmin.setCreateTime(LocalDateTime.now());
        superAdmin.setRemark("系统默认超级管理员");
        adminList.add(superAdmin);
        
        Admin normalAdmin = new Admin();
        normalAdmin.setId(2L);
        normalAdmin.setUsername("manager");
        normalAdmin.setPassword(passwordEncoder.encode("123456"));
        normalAdmin.setRealName("普通管理员");
        normalAdmin.setEmail("manager@mall.com");
        normalAdmin.setPhone("13800138001");
        normalAdmin.setRole("admin");
        normalAdmin.setStatus(1);
        normalAdmin.setLoginCount(0);
        normalAdmin.setCreateTime(LocalDateTime.now());
        normalAdmin.setRemark("系统默认普通管理员");
        adminList.add(normalAdmin);
        
        log.info("初始化默认管理员数据完成，共{}个管理员", adminList.size());
    }
    
    @Override
    public String login(AdminLoginRequest request) {
        log.info("管理员登录请求：{}", request.getUsername());
        
        // 查找管理员
        Admin admin = findByUsername(request.getUsername());
        if (admin == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查状态
        if (!Integer.valueOf(1).equals(admin.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        
        // 验证密码
        if (!verifyPassword(request.getPassword(), admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 更新登录信息
        updateLastLoginInfo(admin.getId(), "127.0.0.1");
        
        // 生成JWT token
        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), admin.getRole());
        
        log.info("管理员{}登录成功", admin.getUsername());
        return token;
    }
    
    @Override
    public Admin findByUsername(String username) {
        return adminList.stream()
                .filter(admin -> username.equals(admin.getUsername()))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public Admin findById(Long id) {
        return adminList.stream()
                .filter(admin -> id.equals(admin.getId()))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public AdminInfoResponse getAdminInfo(Long adminId) {
        Admin admin = findById(adminId);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        
        AdminInfoResponse response = new AdminInfoResponse();
        BeanUtils.copyProperties(admin, response);
        response.setRoleText(admin.getRoleText());
        response.setStatusText(admin.getStatusText());
        
        return response;
    }
    
    @Override
    public void updateLastLoginInfo(Long adminId, String ip) {
        Admin admin = findById(adminId);
        if (admin != null) {
            admin.setLastLoginTime(LocalDateTime.now());
            admin.setLastLoginIp(ip);
            admin.setLoginCount(admin.getLoginCount() + 1);
            log.info("更新管理员{}登录信息", admin.getUsername());
        }
    }
    
    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}