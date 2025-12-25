package com.mall.merchant.controller;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.merchant.domain.entity.Merchant;
import com.mall.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 商家管理控制器
 * 提供商家注册、登录、信息管理、认证等功能的REST API接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
@Validated
@Tag(name = "商家管理", description = "商家注册、登录、信息管理、认证等功能")
public class MerchantController {

    private static final Logger log = LoggerFactory.getLogger(MerchantController.class);

    private final MerchantService merchantService;

    /**
     * 商家注册
     * 新商家注册账号，需要提供基本信息
     * 
     * @param merchant 商家注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "商家注册", description = "新商家注册账号")
    public R<Void> register(@Valid @RequestBody Merchant merchant) {
        log.info("商家注册请求，用户名：{}", merchant.getUsername());
        return merchantService.register(merchant);
    }

    /**
     * 商家登录
     * 使用用户名和密码进行登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含商家信息和token
     */
    @PostMapping("/login")
    @Operation(summary = "商家登录", description = "使用用户名和密码进行登录")
    public R<Map<String, Object>> login(
            @Parameter(description = "用户名") @RequestParam @NotBlank String username,
            @Parameter(description = "密码") @RequestParam @NotBlank String password) {
        log.info("商家登录请求，用户名：{}", username);
        return merchantService.login(username, password);
    }

    /**
     * 商家登出
     * 退出登录，清除登录状态
     * 
     * @param merchantId 商家ID
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "商家登出", description = "退出登录")
    public R<Void> logout(@Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.info("商家登出请求，商家ID：{}", merchantId);
        return merchantService.logout(merchantId);
    }

    /**
     * 获取商家信息
     * 根据商家ID获取详细信息
     * 
     * @param merchantId 商家ID
     * @return 商家信息
     */
    @GetMapping("/{merchantId}")
    @Operation(summary = "获取商家信息", description = "根据商家ID获取详细信息")
    public R<Merchant> getMerchantById(@Parameter(description = "商家ID") @PathVariable @NotNull Long merchantId) {
        log.debug("获取商家信息请求，商家ID：{}", merchantId);
        return merchantService.getMerchantById(merchantId);
    }

    /**
     * 更新商家基本信息
     * 更新商家的基本信息，如店铺名称、联系方式等
     * 
     * @param merchantId 商家ID
     * @param merchant   更新的商家信息
     * @return 更新结果
     */
    @PutMapping("/{merchantId}")
    @Operation(summary = "更新商家信息", description = "更新商家的基本信息")
    public R<Void> updateMerchant(
            @Parameter(description = "商家ID") @PathVariable @NotNull Long merchantId,
            @Valid @RequestBody Merchant merchant) {
        log.info("更新商家信息请求，商家ID：{}", merchantId);
        merchant.setId(merchantId);
        return merchantService.updateMerchantInfo(merchant);
    }

    /**
     * 更新商家密码
     * 修改商家登录密码
     * 
     * @param merchantId  商家ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 更新结果
     */
    @PutMapping("/{merchantId}/password")
    @Operation(summary = "更新商家密码", description = "修改商家登录密码")
    public R<Void> updatePassword(
            @Parameter(description = "商家ID") @PathVariable @NotNull Long merchantId,
            @Parameter(description = "原密码") @RequestParam @NotBlank String oldPassword,
            @Parameter(description = "新密码") @RequestParam @NotBlank String newPassword) {
        log.info("更新商家密码请求，商家ID：{}", merchantId);
        return merchantService.updatePassword(merchantId, oldPassword, newPassword);
    }

    /**
     * 更新商家头像
     * 上传并更新商家头像
     * 
     * @param merchantId 商家ID
     * @param avatarFile 头像文件
     * @return 更新结果
     */
    @PostMapping("/{merchantId}/avatar")
    @Operation(summary = "更新商家头像", description = "上传并更新商家头像")
    public R<Void> updateAvatar(
            @Parameter(description = "商家ID") @PathVariable @NotNull Long merchantId,
            @Parameter(description = "头像文件") @RequestParam("avatar") MultipartFile avatarFile) {
        log.info("更新商家头像请求，商家ID：{}", merchantId);
        // TODO: 实现文件上传逻辑，获取头像URL
        String avatarUrl = ""; // 这里应该是文件上传后的URL
        return merchantService.updateAvatar(merchantId, avatarUrl);
    }

    /**
     * 提交认证信息
     * 商家提交身份认证和营业执照等认证材料
     * 
     * @param merchantId 商家ID
     * @param merchant   认证信息
     * @return 提交结果
     */
    @PostMapping("/{merchantId}/certification")
    @Operation(summary = "提交认证信息", description = "商家提交身份认证和营业执照等认证材料")
    public R<Void> submitCertification(
            @Parameter(description = "商家ID") @PathVariable @NotNull Long merchantId,
            @RequestBody Merchant merchant) {
        log.info("提交认证信息请求，商家ID：{}", merchantId);
        return merchantService.submitAuthentication(merchantId, merchant);
    }

    /**
     * 获取认证状态
     * 查询商家的认证审核状态
     * 
     * @param merchantId 商家ID
     * @return 认证状态信息
     */
    @GetMapping("/{merchantId}/certification/status")
    @Operation(summary = "获取认证状态", description = "查询商家的认证审核状态")
    public R<Map<String, Object>> getCertificationStatus(
            @Parameter(description = "商家ID") @PathVariable @NotNull Long merchantId) {
        log.debug("获取认证状态请求，商家ID：{}", merchantId);
        return merchantService.getAuthenticationStatus(merchantId);
    }

    /**
     * 分页查询商家列表
     * 管理员功能：分页查询商家列表，支持多条件筛选
     * 
     * @param page           页码
     * @param size           每页大小
     * @param shopName       店铺名称（可选）
     * @param auditStatus    审核状态（可选）
     * @param merchantStatus 商家状态（可选）
     * @param merchantType   商家类型（可选）
     * @return 商家分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询商家列表", description = "管理员功能：分页查询商家列表")
    public R<PageResult<Merchant>> getMerchantList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "店铺名称") @RequestParam(required = false) String shopName,
            @Parameter(description = "审核状态") @RequestParam(required = false) Integer auditStatus,
            @Parameter(description = "商家状态") @RequestParam(required = false) Integer merchantStatus,
            @Parameter(description = "商家类型") @RequestParam(required = false) Integer merchantType) {
        log.debug("分页查询商家列表请求，页码：{}，大小：{}", page, size);
        return merchantService.getMerchantList(page, size, shopName, merchantType, auditStatus, merchantStatus);
    }

    /**
     * 审核商家
     * 管理员功能：审核商家的认证申请
     * 
     * @param merchantId  商家ID
     * @param auditStatus 审核状态：1-通过，2-拒绝
     * @param auditRemark 审核备注
     * @return 审核结果
     */
    @PostMapping("/{merchantId}/audit")
    @Operation(summary = "审核商家", description = "管理员功能：审核商家的认证申请")
    public R<Void> auditMerchant(
            @Parameter(description = "商家ID") @PathVariable @NotNull Long merchantId,
            @Parameter(description = "审核状态") @RequestParam @NotNull Integer auditStatus,
            @Parameter(description = "审核备注") @RequestParam(required = false) String auditRemark) {
        log.info("审核商家请求，商家ID：{}，审核状态：{}", merchantId, auditStatus);
        return merchantService.auditMerchant(merchantId, auditStatus, auditRemark);
    }

    /**
     * 禁用商家
     * 管理员功能：禁用违规商家
     * 
     * @param merchantId 商家ID
     * @param reason     禁用原因
     * @return 禁用结果
     */
    @PostMapping("/{merchantId}/disable")
    @Operation(summary = "禁用商家", description = "管理员功能：禁用违规商家")
    public R<Void> disableMerchant(
            @Parameter(description = "商家ID") @PathVariable @NotNull Long merchantId,
            @Parameter(description = "禁用原因") @RequestParam(required = false) String reason) {
        log.info("禁用商家请求，商家ID：{}", merchantId);
        return merchantService.disableMerchant(merchantId, reason);
    }

    /**
     * 启用商家
     * 管理员功能：重新启用被禁用的商家
     * 
     * @param merchantId 商家ID
     * @return 启用结果
     */
    @PostMapping("/{merchantId}/enable")
    @Operation(summary = "启用商家", description = "管理员功能：重新启用被禁用的商家")
    public R<Void> enableMerchant(@Parameter(description = "商家ID") @PathVariable @NotNull Long merchantId) {
        log.info("启用商家请求，商家ID：{}", merchantId);
        return merchantService.enableMerchant(merchantId);
    }

    /**
     * 删除商家
     * 管理员功能：删除商家账号（软删除）
     * 
     * @param merchantId 商家ID
     * @return 删除结果
     */
    @DeleteMapping("/{merchantId}")
    @Operation(summary = "删除商家", description = "管理员功能：删除商家账号")
    public R<Void> deleteMerchant(@Parameter(description = "商家ID") @PathVariable @NotNull Long merchantId) {
        log.info("删除商家请求，商家ID：{}", merchantId);
        return merchantService.deleteMerchant(merchantId);
    }

    /**
     * 获取商家统计数据
     * 获取商家的基本统计信息
     * 
     * @return 统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取商家统计数据", description = "获取商家的基本统计信息")
    public R<Map<String, Object>> getMerchantStatistics() {
        log.info("获取商家统计数据请求");
        return merchantService.getMerchantStatistics();
    }

    /**
     * 导出商家数据
     * 管理员功能：导出商家数据到Excel
     * 
     * @param shopName       店铺名称（可选）
     * @param auditStatus    审核状态（可选）
     * @param merchantStatus 商家状态（可选）
     * @param merchantType   商家类型（可选）
     * @return Excel文件数据
     */
    @GetMapping("/export")
    @Operation(summary = "导出商家数据", description = "管理员功能：导出商家数据到Excel")
    public R<byte[]> exportMerchantData(
            @Parameter(description = "店铺名称") @RequestParam(required = false) String shopName,
            @Parameter(description = "审核状态") @RequestParam(required = false) Integer auditStatus,
            @Parameter(description = "商家状态") @RequestParam(required = false) Integer merchantStatus,
            @Parameter(description = "商家类型") @RequestParam(required = false) Integer merchantType) {
        log.info("导出商家数据请求");
        return merchantService.exportMerchantData(shopName, merchantType, auditStatus, merchantStatus);
    }

    /**
     * 验证密码
     * 验证商家当前密码是否正确
     * 
     * @param merchantId 商家ID
     * @param password   密码
     * @return 验证结果
     */
    @PostMapping("/{merchantId}/verify-password")
    @Operation(summary = "验证密码", description = "验证商家当前密码是否正确")
    public R<Boolean> verifyPassword(
            @Parameter(description = "商家ID") @PathVariable @NotNull Long merchantId,
            @Parameter(description = "密码") @RequestParam @NotBlank String password) {
        log.debug("验证密码请求，商家ID：{}", merchantId);
        return merchantService.verifyPassword(merchantId, password);
    }

    /**
     * 检查用户名是否可用
     * 注册时检查用户名是否已被使用
     * 
     * @param username 用户名
     * @return 检查结果
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名可用性", description = "注册时检查用户名是否已被使用")
    public R<Boolean> checkUsernameAvailable(@Parameter(description = "用户名") @RequestParam @NotBlank String username) {
        log.debug("检查用户名可用性请求，用户名：{}", username);
        return merchantService.checkUsernameAvailable(username);
    }

    /**
     * 检查店铺名称是否可用
     * 注册时检查店铺名称是否已被使用
     * 
     * @param shopName 店铺名称
     * @return 检查结果
     */
    @GetMapping("/check-shop-name")
    @Operation(summary = "检查店铺名称可用性", description = "注册时检查店铺名称是否已被使用")
    public R<Boolean> checkShopNameAvailable(@Parameter(description = "店铺名称") @RequestParam @NotBlank String shopName) {
        log.debug("检查店铺名称可用性请求，店铺名称：{}", shopName);
        return merchantService.checkShopNameAvailable(shopName);
    }

    /**
     * 检查手机号是否可用
     * 注册时检查手机号是否已被使用
     * 
     * @param phone 手机号
     * @return 检查结果
     */
    @GetMapping("/check-phone")
    @Operation(summary = "检查手机号可用性", description = "注册时检查手机号是否已被使用")
    public R<Boolean> checkPhoneAvailable(@Parameter(description = "手机号") @RequestParam @NotBlank String phone) {
        log.debug("检查手机号可用性请求，手机号：{}", phone);
        return merchantService.checkPhoneAvailable(phone);
    }

    /**
     * 检查邮箱是否可用
     * 注册时检查邮箱是否已被使用
     * 
     * @param email 邮箱
     * @return 检查结果
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱可用性", description = "注册时检查邮箱是否已被使用")
    public R<Boolean> checkEmailAvailable(@Parameter(description = "邮箱") @RequestParam @NotBlank String email) {
        log.debug("检查邮箱可用性请求，邮箱：{}", email);
        return merchantService.checkEmailAvailable(email);
    }

    /**
     * 重置密码
     * 通过手机号重置密码
     * 
     * @param phone            手机号
     * @param verificationCode 验证码
     * @param newPassword      新密码
     * @return 重置结果
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "通过手机号重置密码")
    public R<Void> resetPassword(
            @Parameter(description = "手机号") @RequestParam @NotBlank String phone,
            @Parameter(description = "验证码") @RequestParam @NotBlank String verificationCode,
            @Parameter(description = "新密码") @RequestParam @NotBlank String newPassword) {
        log.info("重置密码请求，手机号：{}", phone);
        return merchantService.resetPassword(phone, verificationCode, newPassword);
    }

    /**
     * 发送验证码
     * 发送手机验证码
     * 
     * @param phone 手机号
     * @param type  验证码类型
     * @return 发送结果
     */
    @PostMapping("/send-verification-code")
    @Operation(summary = "发送验证码", description = "发送手机验证码")
    public R<Void> sendVerificationCode(
            @Parameter(description = "手机号") @RequestParam @NotBlank String phone,
            @Parameter(description = "验证码类型") @RequestParam @NotNull Integer type) {
        log.info("发送验证码请求，手机号：{}，类型：{}", phone, type);
        return merchantService.sendVerifyCode(phone, type);
    }
}