package com.mall.payment.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 安全工具类
 * 提供权限检查和用户信息获取的便捷方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
public class SecurityUtils {

    /**
     * 获取当前认证用户的ID
     * 
     * @return 用户ID，如果未认证则返回null
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 获取当前认证用户的角色列表
     * 
     * @return 角色列表，不包含ROLE_前缀
     */
    public static List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    /**
     * 获取当前认证用户的权限列表
     * 
     * @return 权限列表，包含完整的权限名称
     */
    public static List<String> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    /**
     * 检查当前用户是否具有指定角色
     * 
     * @param role 角色名称（不需要ROLE_前缀）
     * @return 是否具有该角色
     */
    public static boolean hasRole(String role) {
        if (!StringUtils.hasText(role)) {
            return false;
        }
        return getCurrentUserRoles().contains(role.toUpperCase());
    }

    /**
     * 检查当前用户是否具有指定角色中的任意一个
     * 
     * @param roles 角色名称数组
     * @return 是否具有其中任意一个角色
     */
    public static boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        
        List<String> userRoles = getCurrentUserRoles();
        for (String role : roles) {
            if (StringUtils.hasText(role) && userRoles.contains(role.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查当前用户是否具有所有指定角色
     * 
     * @param roles 角色名称数组
     * @return 是否具有所有角色
     */
    public static boolean hasAllRoles(String... roles) {
        if (roles == null || roles.length == 0) {
            return true;
        }
        
        List<String> userRoles = getCurrentUserRoles();
        for (String role : roles) {
            if (!StringUtils.hasText(role) || !userRoles.contains(role.toUpperCase())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查当前用户是否具有指定权限
     * 
     * @param authority 权限名称
     * @return 是否具有该权限
     */
    public static boolean hasAuthority(String authority) {
        if (!StringUtils.hasText(authority)) {
            return false;
        }
        return getCurrentUserAuthorities().contains(authority);
    }

    /**
     * 检查当前用户是否具有指定权限中的任意一个
     * 
     * @param authorities 权限名称数组
     * @return 是否具有其中任意一个权限
     */
    public static boolean hasAnyAuthority(String... authorities) {
        if (authorities == null || authorities.length == 0) {
            return false;
        }
        
        List<String> userAuthorities = getCurrentUserAuthorities();
        for (String authority : authorities) {
            if (StringUtils.hasText(authority) && userAuthorities.contains(authority)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查当前用户是否为管理员
     * 
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        return hasAnyRole("ADMIN", "SUPER_ADMIN");
    }

    /**
     * 检查当前用户是否为超级管理员
     * 
     * @return 是否为超级管理员
     */
    public static boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }

    /**
     * 检查当前用户是否为普通用户
     * 
     * @return 是否为普通用户
     */
    public static boolean isUser() {
        return hasRole("USER") && !isAdmin();
    }

    /**
     * 检查当前用户是否已认证
     * 
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 检查当前用户是否为匿名用户
     * 
     * @return 是否为匿名用户
     */
    public static boolean isAnonymous() {
        return !isAuthenticated();
    }

    /**
     * 检查当前用户是否为资源所有者
     * 
     * @param resourceUserId 资源所有者ID
     * @return 是否为资源所有者
     */
    public static boolean isOwner(String resourceUserId) {
        String currentUserId = getCurrentUserId();
        return StringUtils.hasText(currentUserId) && 
               StringUtils.hasText(resourceUserId) && 
               currentUserId.equals(resourceUserId);
    }

    /**
     * 检查当前用户是否可以访问资源
     * 管理员可以访问所有资源，普通用户只能访问自己的资源
     * 
     * @param resourceUserId 资源所有者ID
     * @return 是否可以访问
     */
    public static boolean canAccess(String resourceUserId) {
        return isAdmin() || isOwner(resourceUserId);
    }

    /**
     * 检查当前用户是否可以修改资源
     * 超级管理员可以修改所有资源，管理员可以修改非管理员的资源，普通用户只能修改自己的资源
     * 
     * @param resourceUserId 资源所有者ID
     * @return 是否可以修改
     */
    public static boolean canModify(String resourceUserId) {
        if (isSuperAdmin()) {
            return true;
        }
        
        if (isAdmin()) {
            // 管理员不能修改其他管理员的资源
            return !isUserAdmin(resourceUserId);
        }
        
        return isOwner(resourceUserId);
    }

    /**
     * 检查指定用户是否为管理员
     * 注意：这个方法需要根据实际的用户服务来实现
     * 
     * @param userId 用户ID
     * @return 是否为管理员
     */
    private static boolean isUserAdmin(String userId) {
        // TODO: 实现用户角色查询逻辑
        // 这里应该调用用户服务来查询指定用户的角色
        return false;
    }

    /**
     * 获取当前认证对象
     * 
     * @return 认证对象
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 清除当前安全上下文
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 获取用户友好的角色名称
     * 
     * @param role 角色名称
     * @return 友好的角色名称
     */
    public static String getFriendlyRoleName(String role) {
        if (!StringUtils.hasText(role)) {
            return "未知";
        }
        
        switch (role.toUpperCase()) {
            case "USER":
                return "普通用户";
            case "ADMIN":
                return "管理员";
            case "SUPER_ADMIN":
                return "超级管理员";
            default:
                return role;
        }
    }

    /**
     * 获取当前用户的友好角色名称列表
     * 
     * @return 友好的角色名称列表
     */
    public static List<String> getCurrentUserFriendlyRoles() {
        return getCurrentUserRoles().stream()
                .map(SecurityUtils::getFriendlyRoleName)
                .collect(Collectors.toList());
    }
}