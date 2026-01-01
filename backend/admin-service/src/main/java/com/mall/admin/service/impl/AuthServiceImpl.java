package com.mall.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.admin.domain.dto.LoginRequest;
import com.mall.admin.domain.entity.Admin;
import com.mall.admin.domain.entity.LoginLog;
import com.mall.admin.domain.entity.Role;
import com.mall.admin.domain.vo.AdminVO;
import com.mall.admin.domain.vo.LoginResponse;
import com.mall.admin.domain.vo.RoleVO;
import com.mall.admin.exception.BusinessException;
import com.mall.admin.exception.UnauthorizedException;
import com.mall.admin.mapper.AdminMapper;
import com.mall.admin.mapper.LoginLogMapper;
import com.mall.admin.mapper.RoleMapper;
import com.mall.admin.service.AuthService;
import com.mall.admin.util.JwtUtil;
import com.mall.admin.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 管理员认证服务实现
 * 
 * @author lingbai
 * @since 2025-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final AdminMapper adminMapper;
    private final LoginLogMapper loginLogMapper;
    private final RoleMapper roleMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    
    private static final String TOKEN_PREFIX = "admin:token:";
    private static final String BLACKLIST_PREFIX = "admin:token:blacklist:";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        // 1. 查询管理员
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getUsername, request.getUsername());
        Admin admin = adminMapper.selectOne(queryWrapper);
        
        // 2. 验证账号是否存在
        if (admin == null) {
            recordLoginFailure(null, request.getUsername(), "账号不存在", ipAddress, userAgent);
            throw new BusinessException("用户名或密码错误");
        }
        
        // 3. 检查账号是否被锁定
        if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(LocalDateTime.now())) {
            recordLoginFailure(admin.getId(), request.getUsername(), "账号已锁定", ipAddress, userAgent);
            throw new BusinessException("账号已被锁定,请稍后再试");
        }
        
        // 4. 检查账号状态
        if (admin.getStatus() == 0) {
            recordLoginFailure(admin.getId(), request.getUsername(), "账号已禁用", ipAddress, userAgent);
            throw new BusinessException("账号已被禁用");
        }
        
        // 5. 验证密码
        if (!PasswordUtil.matches(request.getPassword(), admin.getPassword())) {
            handleLoginFailure(admin, ipAddress, userAgent);
            throw new BusinessException("用户名或密码错误");
        }
        
        // 6. 登录成功,重置失败次数
        admin.setFailedLoginCount(0);
        admin.setLockedUntil(null);
        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp(ipAddress);
        adminMapper.updateById(admin);
        
        // 7. 记录登录成功日志
        recordLoginSuccess(admin.getId(), request.getUsername(), ipAddress, userAgent);
        
        // 8. 生成Token
        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername());
        
        // 9. 将Token存入Redis
        String tokenKey = TOKEN_PREFIX + admin.getId();
        redisTemplate.opsForValue().set(tokenKey, token, 24, TimeUnit.HOURS);
        
        // 10. 查询角色和权限
        List<Role> roles = roleMapper.selectRolesByAdminId(admin.getId());
        List<String> permissions = adminMapper.selectPermissionsByAdminId(admin.getId());
        
        // 11. 构造响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setAdminInfo(buildAdminVO(admin, roles, permissions));
        
        log.info("管理员登录成功: username={}, ip={}", admin.getUsername(), ipAddress);
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(String token, Long adminId) {
        // 1. 将Token加入黑名单
        String blacklistKey = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(blacklistKey, "1", 24, TimeUnit.HOURS);
        
        // 2. 删除Redis中的Token
        String tokenKey = TOKEN_PREFIX + adminId;
        redisTemplate.delete(tokenKey);
        
        log.info("管理员登出成功: adminId={}", adminId);
    }
    
    @Override
    public String refreshToken(String oldToken) {
        // 1. 验证旧Token
        if (!jwtUtil.validateToken(oldToken)) {
            throw new UnauthorizedException("Token已失效");
        }
        
        // 2. 检查是否在黑名单中
        String blacklistKey = BLACKLIST_PREFIX + oldToken;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            throw new UnauthorizedException("Token已失效");
        }
        
        // 3. 从旧Token中获取信息
        Long adminId = jwtUtil.getAdminIdFromToken(oldToken);
        String username = jwtUtil.getUsernameFromToken(oldToken);
        
        // 4. 生成新Token
        String newToken = jwtUtil.generateToken(adminId, username);
        
        // 5. 更新Redis中的Token
        String tokenKey = TOKEN_PREFIX + adminId;
        redisTemplate.opsForValue().set(tokenKey, newToken, 24, TimeUnit.HOURS);
        
        // 6. 将旧Token加入黑名单
        redisTemplate.opsForValue().set(blacklistKey, "1", 24, TimeUnit.HOURS);
        
        log.info("Token刷新成功: adminId={}", adminId);
        return newToken;
    }
    
    @Override
    public boolean validateToken(String token) {
        // 1. 验证Token格式
        if (!jwtUtil.validateToken(token)) {
            return false;
        }
        
        // 2. 检查是否在黑名单中
        String blacklistKey = BLACKLIST_PREFIX + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            return false;
        }
        
        // 3. 检查Redis中是否存在
        Long adminId = jwtUtil.getAdminIdFromToken(token);
        String tokenKey = TOKEN_PREFIX + adminId;
        String storedToken = redisTemplate.opsForValue().get(tokenKey);
        
        return token.equals(storedToken);
    }
    
    /**
     * 处理登录失败
     */
    private void handleLoginFailure(Admin admin, String ipAddress, String userAgent) {
        // 增加失败次数
        int failedCount = admin.getFailedLoginCount() + 1;
        admin.setFailedLoginCount(failedCount);
        
        // 如果失败次数达到上限,锁定账号
        if (failedCount >= MAX_FAILED_ATTEMPTS) {
            admin.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            adminMapper.updateById(admin);
            recordLoginFailure(admin.getId(), admin.getUsername(), 
                "密码错误次数过多,账号已锁定" + LOCK_DURATION_MINUTES + "分钟", ipAddress, userAgent);
        } else {
            adminMapper.updateById(admin);
            recordLoginFailure(admin.getId(), admin.getUsername(), 
                "密码错误", ipAddress, userAgent);
        }
    }
    
    /**
     * 记录登录成功日志
     */
    private void recordLoginSuccess(Long adminId, String username, String ipAddress, String userAgent) {
        LoginLog log = new LoginLog();
        log.setAdminId(adminId);
        log.setUsername(username);
        log.setLoginResult(1);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setCreatedAt(LocalDateTime.now());
        loginLogMapper.insert(log);
    }
    
    /**
     * 记录登录失败日志
     */
    private void recordLoginFailure(Long adminId, String username, String reason, String ipAddress, String userAgent) {
        LoginLog log = new LoginLog();
        log.setAdminId(adminId);
        log.setUsername(username);
        log.setLoginResult(0);
        log.setFailureReason(reason);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setCreatedAt(LocalDateTime.now());
        loginLogMapper.insert(log);
    }
    
    /**
     * 构建AdminVO
     */
    private AdminVO buildAdminVO(Admin admin, List<Role> roles, List<String> permissions) {
        AdminVO adminVO = new AdminVO();
        BeanUtils.copyProperties(admin, adminVO);
        
        // 设置角色列表
        if (roles != null && !roles.isEmpty()) {
            List<RoleVO> roleVOs = roles.stream().map(role -> {
                RoleVO roleVO = new RoleVO();
                BeanUtils.copyProperties(role, roleVO);
                return roleVO;
            }).collect(Collectors.toList());
            adminVO.setRoles(roleVOs);
        }
        
        // 设置权限列表
        adminVO.setPermissions(permissions);
        
        return adminVO;
    }
}
