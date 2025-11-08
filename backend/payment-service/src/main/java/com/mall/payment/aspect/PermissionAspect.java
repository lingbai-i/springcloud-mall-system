package com.mall.payment.aspect;

import com.mall.payment.annotation.RequirePermission;
import com.mall.payment.util.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

/**
 * 权限检查切面
 * 用于处理@RequirePermission注解的权限验证
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Aspect
@Component
@Order(1) // 确保在其他切面之前执行
public class PermissionAspect {

    private static final Logger logger = LoggerFactory.getLogger(PermissionAspect.class);

    /**
     * 权限检查前置通知
     * 在执行带有@RequirePermission注解的方法前进行权限验证
     * 
     * @param joinPoint 连接点
     * @param requirePermission 权限注解
     * @throws AccessDeniedException 权限不足异常
     */
    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        logger.debug("开始执行权限检查，方法：{}", joinPoint.getSignature().getName());
        
        try {
            // 检查用户是否已认证
            if (!SecurityUtils.isAuthenticated()) {
                logger.warn("用户未认证，拒绝访问方法：{}", joinPoint.getSignature().getName());
                throw new AccessDeniedException("用户未认证");
            }

            String currentUserId = SecurityUtils.getCurrentUserId();
            String[] requiredPermissions = requirePermission.value();
            
            // 如果没有指定权限要求，则只需要认证即可
            if (requiredPermissions.length == 0) {
                logger.debug("方法 {} 无特定权限要求，用户已认证，允许访问", joinPoint.getSignature().getName());
                return;
            }

            // 检查权限
            boolean hasPermission = checkUserPermissions(requiredPermissions, requirePermission.requireAll());
            
            // 如果权限检查失败，检查是否允许资源所有者访问
            if (!hasPermission && requirePermission.allowOwner()) {
                hasPermission = checkOwnerPermission(joinPoint, requirePermission.ownerField(), currentUserId);
            }

            if (!hasPermission) {
                String message = StringUtils.hasText(requirePermission.message()) ? 
                    requirePermission.message() : "权限不足，无法访问该资源";
                
                logger.warn("用户 {} 权限不足，无法访问方法：{}，所需权限：{}", 
                    currentUserId, joinPoint.getSignature().getName(), Arrays.toString(requiredPermissions));
                
                throw new AccessDeniedException(message);
            }

            logger.debug("用户 {} 权限检查通过，允许访问方法：{}", currentUserId, joinPoint.getSignature().getName());
            
        } catch (AccessDeniedException e) {
            // 重新抛出访问拒绝异常
            throw e;
        } catch (Exception e) {
            logger.error("权限检查过程中发生异常，方法：{}", joinPoint.getSignature().getName(), e);
            throw new AccessDeniedException("权限检查失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户权限
     * 
     * @param requiredPermissions 所需权限数组
     * @param requireAll 是否需要所有权限
     * @return 是否有权限
     */
    private boolean checkUserPermissions(String[] requiredPermissions, boolean requireAll) {
        List<String> userAuthorities = SecurityUtils.getCurrentUserAuthorities();
        List<String> userRoles = SecurityUtils.getCurrentUserRoles();
        
        logger.debug("用户权限：{}，用户角色：{}", userAuthorities, userRoles);
        logger.debug("所需权限：{}，需要全部：{}", Arrays.toString(requiredPermissions), requireAll);
        
        if (requireAll) {
            // 需要拥有所有权限
            return Arrays.stream(requiredPermissions)
                    .allMatch(permission -> hasPermissionOrRole(permission, userAuthorities, userRoles));
        } else {
            // 只需要拥有其中一个权限
            return Arrays.stream(requiredPermissions)
                    .anyMatch(permission -> hasPermissionOrRole(permission, userAuthorities, userRoles));
        }
    }

    /**
     * 检查用户是否拥有指定权限或角色
     * 
     * @param permission 权限或角色名称
     * @param userAuthorities 用户权限列表
     * @param userRoles 用户角色列表
     * @return 是否拥有权限或角色
     */
    private boolean hasPermissionOrRole(String permission, List<String> userAuthorities, List<String> userRoles) {
        // 检查是否为角色（以ROLE_开头或在角色列表中）
        if (permission.startsWith("ROLE_")) {
            String roleName = permission.substring(5);
            return userRoles.contains(roleName) || userAuthorities.contains(permission);
        }
        
        // 检查角色（不带ROLE_前缀）
        if (userRoles.contains(permission.toUpperCase())) {
            return true;
        }
        
        // 检查权限
        return userAuthorities.contains(permission);
    }

    /**
     * 检查资源所有者权限
     * 
     * @param joinPoint 连接点
     * @param ownerField 所有者字段名
     * @param currentUserId 当前用户ID
     * @return 是否为资源所有者
     */
    private boolean checkOwnerPermission(JoinPoint joinPoint, String ownerField, String currentUserId) {
        try {
            Object[] args = joinPoint.getArgs();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Parameter[] parameters = method.getParameters();
            
            // 查找包含所有者字段的参数
            for (int i = 0; i < parameters.length && i < args.length; i++) {
                Object arg = args[i];
                if (arg != null) {
                    String resourceUserId = extractOwnerIdFromObject(arg, ownerField);
                    if (resourceUserId != null) {
                        boolean isOwner = SecurityUtils.isOwner(resourceUserId);
                        logger.debug("资源所有者检查：当前用户 {}，资源所有者 {}，是否匹配：{}", 
                            currentUserId, resourceUserId, isOwner);
                        return isOwner;
                    }
                }
            }
            
            logger.debug("未找到资源所有者信息，字段名：{}", ownerField);
            return false;
            
        } catch (Exception e) {
            logger.error("检查资源所有者权限时发生异常", e);
            return false;
        }
    }

    /**
     * 从对象中提取所有者ID
     * 
     * @param obj 对象
     * @param ownerField 所有者字段名
     * @return 所有者ID
     */
    private String extractOwnerIdFromObject(Object obj, String ownerField) {
        try {
            // 如果对象本身就是字符串类型的ID
            if (obj instanceof String && "id".equals(ownerField)) {
                return (String) obj;
            }
            
            // 通过反射获取字段值
            Class<?> clazz = obj.getClass();
            
            // 尝试通过getter方法获取
            String getterName = "get" + capitalize(ownerField);
            try {
                Method getter = clazz.getMethod(getterName);
                Object value = getter.invoke(obj);
                return value != null ? value.toString() : null;
            } catch (NoSuchMethodException e) {
                // 如果没有getter方法，尝试直接访问字段
                try {
                    java.lang.reflect.Field field = clazz.getDeclaredField(ownerField);
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    return value != null ? value.toString() : null;
                } catch (NoSuchFieldException | IllegalAccessException ex) {
                    logger.debug("无法从对象 {} 中获取字段 {} 的值", clazz.getSimpleName(), ownerField);
                    return null;
                }
            }
            
        } catch (Exception e) {
            logger.error("提取所有者ID时发生异常", e);
            return null;
        }
    }

    /**
     * 首字母大写
     * 
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    private String capitalize(String str) {
        if (!StringUtils.hasText(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}