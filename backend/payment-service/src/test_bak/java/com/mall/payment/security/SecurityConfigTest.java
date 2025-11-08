package com.mall.payment.security;

import com.mall.payment.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecurityConfig安全配置测试
 * 测试Spring Security的权限控制配置
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-01
 */
@SpringBootTest
@AutoConfigureTestMvc
@DisplayName("安全配置测试")
class SecurityConfigTest extends BaseTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("公开接口 - 无需认证")
    void testPublicEndpoints_NoAuthRequired() throws Exception {
        // 测试健康检查接口
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        
        // 测试支付回调接口（支付宝）
        mockMvc.perform(post("/api/payment/callback/alipay"))
                .andExpect(status().isOk());
        
        // 测试支付回调接口（微信）
        mockMvc.perform(post("/api/payment/callback/wechat"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("用户接口 - 需要USER角色")
    @WithMockUser(roles = "USER")
    void testUserEndpoints_RequireUserRole() throws Exception {
        // 测试创建支付订单接口
        mockMvc.perform(post("/api/payment/orders"))
                .andExpect(status().isBadRequest()); // 参数验证失败，但通过了权限验证
        
        // 测试发起支付接口
        mockMvc.perform(post("/api/payment/orders/PAY_TEST/initiate"))
                .andExpect(status().isNotFound()); // 订单不存在，但通过了权限验证
    }
    
    @Test
    @DisplayName("用户接口 - 未认证用户被拒绝")
    void testUserEndpoints_UnauthenticatedUserDenied() throws Exception {
        // 测试创建支付订单接口 - 未认证
        mockMvc.perform(post("/api/payment/orders"))
                .andExpect(status().isUnauthorized());
        
        // 测试发起支付接口 - 未认证
        mockMvc.perform(post("/api/payment/orders/PAY_TEST/initiate"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("用户接口 - 错误角色被拒绝")
    @WithMockUser(roles = "GUEST")
    void testUserEndpoints_WrongRoleDenied() throws Exception {
        // 测试创建支付订单接口 - 错误角色
        mockMvc.perform(post("/api/payment/orders"))
                .andExpect(status().isForbidden());
        
        // 测试发起支付接口 - 错误角色
        mockMvc.perform(post("/api/payment/orders/PAY_TEST/initiate"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("管理员接口 - 需要ADMIN角色")
    @WithMockUser(roles = "ADMIN")
    void testAdminEndpoints_RequireAdminRole() throws Exception {
        // 测试分页查询支付订单接口
        mockMvc.perform(get("/api/payment/orders"))
                .andExpect(status().isOk());
        
        // 测试查询风控记录接口
        mockMvc.perform(get("/api/risk/records"))
                .andExpect(status().isOk());
        
        // 测试获取支付统计数据接口
        mockMvc.perform(get("/api/statistics/overview"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("管理员接口 - 普通用户被拒绝")
    @WithMockUser(roles = "USER")
    void testAdminEndpoints_UserRoleDenied() throws Exception {
        // 测试分页查询支付订单接口 - 普通用户被拒绝
        mockMvc.perform(get("/api/payment/orders"))
                .andExpect(status().isForbidden());
        
        // 测试查询风控记录接口 - 普通用户被拒绝
        mockMvc.perform(get("/api/risk/records"))
                .andExpect(status().isForbidden());
        
        // 测试获取支付统计数据接口 - 普通用户被拒绝
        mockMvc.perform(get("/api/statistics/overview"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("管理员接口 - 未认证用户被拒绝")
    void testAdminEndpoints_UnauthenticatedUserDenied() throws Exception {
        // 测试分页查询支付订单接口 - 未认证
        mockMvc.perform(get("/api/payment/orders"))
                .andExpect(status().isUnauthorized());
        
        // 测试查询风控记录接口 - 未认证
        mockMvc.perform(get("/api/risk/records"))
                .andExpect(status().isUnauthorized());
        
        // 测试获取支付统计数据接口 - 未认证
        mockMvc.perform(get("/api/statistics/overview"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("超级管理员接口 - 需要SUPER_ADMIN角色")
    @WithMockUser(roles = "SUPER_ADMIN")
    void testSuperAdminEndpoints_RequireSuperAdminRole() throws Exception {
        // 测试审核风控记录接口
        mockMvc.perform(post("/api/risk/records/RISK_001/review"))
                .andExpect(status().isBadRequest()); // 参数验证失败，但通过了权限验证
    }
    
    @Test
    @DisplayName("超级管理员接口 - 普通管理员被拒绝")
    @WithMockUser(roles = "ADMIN")
    void testSuperAdminEndpoints_AdminRoleDenied() throws Exception {
        // 测试审核风控记录接口 - 普通管理员被拒绝
        mockMvc.perform(post("/api/risk/records/RISK_001/review"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("超级管理员接口 - 普通用户被拒绝")
    @WithMockUser(roles = "USER")
    void testSuperAdminEndpoints_UserRoleDenied() throws Exception {
        // 测试审核风控记录接口 - 普通用户被拒绝
        mockMvc.perform(post("/api/risk/records/RISK_001/review"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("混合权限接口 - USER和ADMIN都可访问")
    @WithMockUser(roles = "USER")
    void testMixedPermissionEndpoints_UserRole() throws Exception {
        // 测试查询支付订单详情接口 - USER角色可访问
        mockMvc.perform(get("/api/payment/orders/PAY_TEST"))
                .andExpect(status().isNotFound()); // 订单不存在，但通过了权限验证
    }
    
    @Test
    @DisplayName("混合权限接口 - USER和ADMIN都可访问")
    @WithMockUser(roles = "ADMIN")
    void testMixedPermissionEndpoints_AdminRole() throws Exception {
        // 测试查询支付订单详情接口 - ADMIN角色可访问
        mockMvc.perform(get("/api/payment/orders/PAY_TEST"))
                .andExpect(status().isNotFound()); // 订单不存在，但通过了权限验证
    }
    
    @Test
    @DisplayName("CORS预检请求 - 允许跨域")
    void testCorsPreflightRequest_Allowed() throws Exception {
        // 测试CORS预检请求
        mockMvc.perform(options("/api/payment/orders")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("不存在的接口 - 返回404")
    @WithMockUser(roles = "ADMIN")
    void testNonExistentEndpoint_Returns404() throws Exception {
        // 测试不存在的接口
        mockMvc.perform(get("/api/non-existent-endpoint"))
                .andExpect(status().isNotFound());
    }
}