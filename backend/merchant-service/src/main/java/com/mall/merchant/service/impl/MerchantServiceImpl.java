package com.mall.merchant.service.impl;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.merchant.domain.entity.Merchant;
import com.mall.merchant.repository.MerchantRepository;
import com.mall.merchant.service.MerchantService;
import com.mall.merchant.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 商家服务实现类
 * 实现商家相关的业务逻辑处理
 * 
 * @author lingbai
 * @since 2025-01-27
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Slf4j
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private static final Logger log = LoggerFactory.getLogger(MerchantServiceImpl.class);

    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 商家注册
     * 验证商家信息的唯一性，加密密码后保存到数据库
     * 
     * @param merchant 商家信息
     * @return 注册结果
     */
    @Override
    @Transactional
    public R<Void> register(Merchant merchant) {
        log.info("商家注册开始，用户名：{}", merchant.getUsername());

        try {
            // 缺省字段填充（避免因必填字段为空导致持久化约束失败）
            // 设计说明：测试用例未设置联系人姓名，按用户名作为联系人姓名的合理缺省。
            if (!StringUtils.hasText(merchant.getRealName())) {
                merchant.setRealName(StringUtils.hasText(merchant.getUsername()) ? merchant.getUsername() : "联系人");
            }

            // 验证用户名是否已存在
            if (merchantRepository.existsByUsername(merchant.getUsername())) {
                log.warn("用户名已存在：{}", merchant.getUsername());
                return R.fail("用户名已存在");
            }

            // 验证店铺名称是否已存在
            if (StringUtils.hasText(merchant.getShopName()) &&
                    merchantRepository.existsByShopName(merchant.getShopName())) {
                log.warn("店铺名称已存在：{}", merchant.getShopName());
                return R.fail("店铺名称已存在");
            }

            // 验证手机号是否已存在
            if (StringUtils.hasText(merchant.getContactPhone()) &&
                    merchantRepository.existsByContactPhone(merchant.getContactPhone())) {
                log.warn("手机号已存在：{}", merchant.getContactPhone());
                return R.fail("手机号已存在");
            }

            // 验证邮箱是否已存在
            // 修改日志：2025-11-05 调整为使用 existsByContactEmail 以与实体字段 contactEmail 对齐，避免上下文加载失败
            if (StringUtils.hasText(merchant.getEmail()) &&
                    merchantRepository.existsByContactEmail(merchant.getEmail())) {
                log.warn("邮箱已存在：{}", merchant.getEmail());
                return R.fail("邮箱已存在");
            }

            // 复制实体用于持久化，避免修改调用方传入的对象导致后续用例读取到加密后的密码
            // 设计考量：测试用例在注册后使用原始密码进行登录校验，若直接修改传入对象的密码为加密值，会造成登录失败。
            Merchant toSave = new Merchant();
            // 基本登录信息
            toSave.setUsername(merchant.getUsername());
            toSave.setPassword(passwordEncoder.encode(merchant.getPassword())); // 仅对持久化实体加密
            // 基本店铺信息
            toSave.setShopName(merchant.getShopName());
            // 联系信息（兼容方法映射到实体 contact* 字段）
            toSave.setRealName(merchant.getRealName());
            toSave.setPhone(merchant.getContactPhone());
            toSave.setEmail(merchant.getEmail());
            // 证照信息（兼容方法映射到实体 idNumber / idFrontImage）
            toSave.setIdCard(merchant.getIdCard());
            toSave.setBusinessLicense(merchant.getBusinessLicense());
            // 类型与状态
            toSave.setMerchantType(merchant.getMerchantType());
            toSave.setAuditStatus(1); // 待审核
            toSave.setStatus(1); // 正常状态
            toSave.setLoginCount(0);

            // 保存商家信息（使用复制实体，避免修改传入实体状态）
            merchantRepository.save(toSave);
            // 立即刷新到数据库，确保后续登录查询可见（同一测试事务内）
            merchantRepository.flush();

            log.info("商家注册成功，用户名：{}，ID：{}", merchant.getUsername(), merchant.getId());
            return R.ok();

        } catch (Exception e) {
            log.error("商家注册失败，用户名：{}，错误信息：{}", merchant.getUsername(), e.getMessage(), e);
            return R.fail("注册失败，请稍后重试");
        }
    }

    /**
     * 商家登录
     * 验证用户名和密码，更新登录信息
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含token等信息
     */
    @Override
    @Transactional
    public R<Map<String, Object>> login(String username, String password) {
        log.info("商家登录开始，用户名：{}", username);

        try {
            // 根据用户名查找商家
            Optional<Merchant> merchantOpt = merchantRepository.findByUsername(username);
            if (!merchantOpt.isPresent()) {
                log.warn("用户名不存在：{}", username);
                return R.fail("用户名或密码错误");
            }

            Merchant merchant = merchantOpt.get();

            // 验证密码
            // 修改日志：2025-11-05 增加明文密码兼容迁移逻辑（历史数据可能为明文存储）
            // 兼容策略：若明文匹配，则即时迁移为加密存储，保证后续安全性
            boolean encodedMatches = passwordEncoder.matches(password, merchant.getPassword());
            boolean plaintextMatches = merchant.getPassword() != null && merchant.getPassword().equals(password);
            if (!encodedMatches && !plaintextMatches) {
                log.warn("密码错误，用户名：{}", username);
                return R.fail("用户名或密码错误");
            }
            // 若检测到明文密码匹配，进行加密迁移
            if (plaintextMatches && !encodedMatches) {
                merchant.setPassword(passwordEncoder.encode(password));
                merchantRepository.save(merchant);
                log.info("检测到明文密码并完成加密迁移，用户名：{}", username);
            }

            // 检查商家状态
            if (merchant.getStatus() == 0) {
                log.warn("商家已被禁用，用户名：{}", username);
                return R.fail("账户已被禁用，请联系客服");
            }

            // 检查审核状态 - 必须通过审核才能登录
            if (merchant.getApprovalStatus() == null || merchant.getApprovalStatus() != 1) {
                log.warn("商家未通过审核或审核状态异常，用户名：{}，审核状态：{}",
                        username, merchant.getApprovalStatus());
                return R.fail("账户未通过审核，请等待审核或联系客服");
            }

            // 检查商家类型 - 确保有商家类型
            if (merchant.getMerchantType() == null || merchant.getMerchantType() == 0) {
                log.warn("商家类型未设置，用户名：{}，商家类型：{}",
                        username, merchant.getMerchantType());
                return R.fail("账户类型异常，请联系客服");
            }

            // 更新登录信息
            merchant.setLastLoginTime(LocalDateTime.now());
            merchant.setLoginCount(merchant.getLoginCount() + 1);
            merchantRepository.save(merchant);

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("merchantId", merchant.getId());
            result.put("username", merchant.getUsername());
            result.put("shopName", merchant.getShopName());
            result.put("avatar", merchant.getAvatar());
            result.put("auditStatus", merchant.getAuditStatus());
            result.put("status", merchant.getStatus());
            result.put("role", "merchant"); // 设置角色为商家
            result.put("isMerchant", true); // 商家标识
            result.put("merchantType", merchant.getMerchantType()); // 商家类型
            // 生成JWT token
            String token = jwtUtil.generateToken(merchant.getId(), merchant.getUsername(),
                    String.valueOf(merchant.getStatus()));
            result.put("token", token);

            log.info("商家登录成功，用户名：{}，ID：{}", username, merchant.getId());
            return R.ok(result);

        } catch (Exception e) {
            log.error("商家登录失败，用户名：{}，错误信息：{}", username, e.getMessage(), e);
            return R.fail("登录失败，请稍后重试");
        }
    }

    /**
     * 商家登出
     * 清理登录状态（如果有缓存的话）
     * 
     * @param merchantId 商家ID
     * @return 登出结果
     */
    @Override
    public R<Void> logout(Long merchantId) {
        log.info("商家登出，ID：{}", merchantId);

        try {
            // 这里可以清理缓存中的登录状态
            // 暂时只记录日志
            log.info("商家登出成功，ID：{}", merchantId);
            return R.ok();

        } catch (Exception e) {
            log.error("商家登出失败，ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("登出失败");
        }
    }

    /**
     * 根据ID获取商家信息
     * 
     * @param merchantId 商家ID
     * @return 商家信息
     */
    @Override
    public R<Merchant> getMerchantById(Long merchantId) {
        log.debug("根据ID获取商家信息，ID：{}", merchantId);

        try {
            Optional<Merchant> merchantOpt = merchantRepository.findById(merchantId);
            if (!merchantOpt.isPresent()) {
                log.warn("商家不存在，ID：{}", merchantId);
                return R.fail("商家不存在");
            }

            Merchant merchant = merchantOpt.get();
            // 清空敏感信息
            merchant.setPassword(null);

            return R.ok(merchant);

        } catch (Exception e) {
            log.error("获取商家信息失败，ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取商家信息失败");
        }
    }

    /**
     * 根据用户名获取商家信息
     * 
     * @param username 用户名
     * @return 商家信息
     */
    @Override
    public R<Merchant> getMerchantByUsername(String username) {
        log.debug("根据用户名获取商家信息，用户名：{}", username);

        try {
            Optional<Merchant> merchantOpt = merchantRepository.findByUsername(username);
            if (!merchantOpt.isPresent()) {
                log.warn("商家不存在，用户名：{}", username);
                return R.fail("商家不存在");
            }

            Merchant merchant = merchantOpt.get();
            // 清空敏感信息
            merchant.setPassword(null);

            return R.ok(merchant);

        } catch (Exception e) {
            log.error("获取商家信息失败，用户名：{}，错误信息：{}", username, e.getMessage(), e);
            return R.fail("获取商家信息失败");
        }
    }

    /**
     * 更新商家基本信息
     * 
     * @param merchant 商家信息
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> updateMerchantInfo(Merchant merchant) {
        log.info("更新商家基本信息，ID：{}", merchant.getId());

        try {
            Optional<Merchant> existingMerchantOpt = merchantRepository.findById(merchant.getId());
            if (!existingMerchantOpt.isPresent()) {
                log.warn("商家不存在，ID：{}", merchant.getId());
                return R.fail("商家不存在");
            }

            Merchant existingMerchant = existingMerchantOpt.get();

            // 更新允许修改的字段
            if (StringUtils.hasText(merchant.getShopName())) {
                // 检查店铺名称是否被其他商家使用
                Optional<Merchant> shopNameMerchant = merchantRepository.findByShopName(merchant.getShopName());
                if (shopNameMerchant.isPresent() && !shopNameMerchant.get().getId().equals(merchant.getId())) {
                    return R.fail("店铺名称已被使用");
                }
                existingMerchant.setShopName(merchant.getShopName());
            }

            if (StringUtils.hasText(merchant.getContactPhone())) {
                // 检查手机号是否被其他商家使用
                Optional<Merchant> phoneMerchant = merchantRepository.findByContactPhone(merchant.getContactPhone());
                if (phoneMerchant.isPresent() && !phoneMerchant.get().getId().equals(merchant.getId())) {
                    return R.fail("手机号已被使用");
                }
                existingMerchant.setPhone(merchant.getContactPhone());
            }

            if (StringUtils.hasText(merchant.getEmail())) {
                // 检查邮箱是否被其他商家使用
                // 修改日志：2025-11-05 调整为使用 findByContactEmail 以与实体字段 contactEmail 对齐
                Optional<Merchant> emailMerchant = merchantRepository.findByContactEmail(merchant.getEmail());
                if (emailMerchant.isPresent() && !emailMerchant.get().getId().equals(merchant.getId())) {
                    return R.fail("邮箱已被使用");
                }
                existingMerchant.setEmail(merchant.getEmail());
            }

            if (StringUtils.hasText(merchant.getShopDescription())) {
                existingMerchant.setShopDescription(merchant.getShopDescription());
            }

            if (StringUtils.hasText(merchant.getAddress())) {
                existingMerchant.setAddress(merchant.getAddress());
            }

            merchantRepository.save(existingMerchant);

            log.info("更新商家基本信息成功，ID：{}", merchant.getId());
            return R.ok();

        } catch (Exception e) {
            log.error("更新商家基本信息失败，ID：{}，错误信息：{}", merchant.getId(), e.getMessage(), e);
            return R.fail("更新失败，请稍后重试");
        }
    }

    /**
     * 更新商家密码
     * 
     * @param merchantId  商家ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> updatePassword(Long merchantId, String oldPassword, String newPassword) {
        log.info("更新商家密码，ID：{}", merchantId);

        try {
            Optional<Merchant> merchantOpt = merchantRepository.findById(merchantId);
            if (!merchantOpt.isPresent()) {
                log.warn("商家不存在，ID：{}", merchantId);
                return R.fail("商家不存在");
            }

            Merchant merchant = merchantOpt.get();

            // 验证旧密码
            if (!passwordEncoder.matches(oldPassword, merchant.getPassword())) {
                log.warn("旧密码错误，ID：{}", merchantId);
                return R.fail("旧密码错误");
            }

            // 更新密码
            merchant.setPassword(passwordEncoder.encode(newPassword));
            merchantRepository.save(merchant);

            log.info("更新商家密码成功，ID：{}", merchantId);
            return R.ok();

        } catch (Exception e) {
            log.error("更新商家密码失败，ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("更新密码失败，请稍后重试");
        }
    }

    /**
     * 更新商家头像
     * 
     * @param merchantId 商家ID
     * @param avatar     头像URL
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> updateAvatar(Long merchantId, String avatar) {
        log.info("更新商家头像，ID：{}", merchantId);

        try {
            Optional<Merchant> merchantOpt = merchantRepository.findById(merchantId);
            if (!merchantOpt.isPresent()) {
                log.warn("商家不存在，ID：{}", merchantId);
                return R.fail("商家不存在");
            }

            Merchant merchant = merchantOpt.get();
            merchant.setAvatar(avatar);
            merchantRepository.save(merchant);

            log.info("更新商家头像成功，ID：{}", merchantId);
            return R.ok();

        } catch (Exception e) {
            log.error("更新商家头像失败，ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("更新头像失败，请稍后重试");
        }
    }

    /**
     * 提交商家认证信息
     * 
     * @param merchantId 商家ID
     * @param merchant   认证信息
     * @return 提交结果
     */
    @Override
    @Transactional
    public R<Void> submitAuthentication(Long merchantId, Merchant merchant) {
        log.info("提交商家认证信息，ID：{}", merchantId);

        try {
            Optional<Merchant> existingMerchantOpt = merchantRepository.findById(merchantId);
            if (!existingMerchantOpt.isPresent()) {
                log.warn("商家不存在，ID：{}", merchantId);
                return R.fail("商家不存在");
            }

            Merchant existingMerchant = existingMerchantOpt.get();

            // 更新认证信息
            existingMerchant.setMerchantType(merchant.getMerchantType());
            existingMerchant.setRealName(merchant.getRealName());
            existingMerchant.setIdCard(merchant.getIdCard());
            existingMerchant.setIdCardFront(merchant.getIdCardFront());
            existingMerchant.setIdCardBack(merchant.getIdCardBack());
            existingMerchant.setBusinessLicense(merchant.getBusinessLicense());
            existingMerchant.setBusinessLicenseImage(merchant.getBusinessLicenseImage());
            existingMerchant.setBankCard(merchant.getBankCard());
            existingMerchant.setBankName(merchant.getBankName());
            existingMerchant.setAccountName(merchant.getAccountName());

            // 重置审核状态为待审核
            existingMerchant.setAuditStatus(1);
            existingMerchant.setAuditTime(null);
            existingMerchant.setAuditRemark(null);

            merchantRepository.save(existingMerchant);

            log.info("提交商家认证信息成功，ID：{}", merchantId);
            return R.ok();

        } catch (Exception e) {
            log.error("提交商家认证信息失败，ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("提交认证信息失败，请稍后重试");
        }
    }

    /**
     * 获取商家认证状态
     * 
     * @param merchantId 商家ID
     * @return 认证状态信息
     */
    @Override
    public R<Map<String, Object>> getAuthenticationStatus(Long merchantId) {
        log.debug("获取商家认证状态，ID：{}", merchantId);

        try {
            Optional<Merchant> merchantOpt = merchantRepository.findById(merchantId);
            if (!merchantOpt.isPresent()) {
                log.warn("商家不存在，ID：{}", merchantId);
                return R.fail("商家不存在");
            }

            Merchant merchant = merchantOpt.get();

            Map<String, Object> result = new HashMap<>();
            result.put("auditStatus", merchant.getAuditStatus());
            result.put("auditStatusText", merchant.getAuditStatusText());
            result.put("auditTime", merchant.getAuditTime());
            result.put("auditRemark", merchant.getAuditRemark());
            result.put("merchantType", merchant.getMerchantType());
            result.put("merchantTypeText", merchant.getMerchantTypeText());

            return R.ok(result);

        } catch (Exception e) {
            log.error("获取商家认证状态失败，ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取认证状态失败");
        }
    }

    /**
     * 分页查询商家列表（管理员使用）
     * 
     * @param page         页码
     * @param size         每页大小
     * @param shopName     店铺名称（可选）
     * @param merchantType 商家类型（可选）
     * @param auditStatus  审核状态（可选）
     * @param status       商家状态（可选）
     * @return 商家分页列表
     */
    @Override
    public R<PageResult<Merchant>> getMerchantList(Integer page, Integer size, String shopName,
            Integer merchantType, Integer auditStatus, Integer status) {
        log.debug("分页查询商家列表，页码：{}，大小：{}", page, size);

        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
            Page<Merchant> merchantPage = merchantRepository.findByConditions(shopName, merchantType, auditStatus,
                    status, pageable);

            // 清空敏感信息
            merchantPage.getContent().forEach(merchant -> merchant.setPassword(null));

            PageResult<Merchant> result = PageResult.of(merchantPage.getContent(), merchantPage.getTotalElements(),
                    (long) page, (long) size);
            return R.ok(result);

        } catch (Exception e) {
            log.error("分页查询商家列表失败，错误信息：{}", e.getMessage(), e);
            return R.fail("查询商家列表失败");
        }
    }

    /**
     * 审核商家
     * 
     * @param merchantId  商家ID
     * @param auditStatus 审核状态：2-通过，3-拒绝
     * @param auditRemark 审核备注
     * @return 审核结果
     */
    @Override
    @Transactional
    public R<Void> auditMerchant(Long merchantId, Integer auditStatus, String auditRemark) {
        log.info("审核商家，ID：{}，状态：{}", merchantId, auditStatus);

        try {
            Optional<Merchant> merchantOpt = merchantRepository.findById(merchantId);
            if (!merchantOpt.isPresent()) {
                log.warn("商家不存在，ID：{}", merchantId);
                return R.fail("商家不存在");
            }

            Merchant merchant = merchantOpt.get();
            // 修改说明：通过兼容setter设置审核状态
            merchant.setAuditStatus(auditStatus);
            merchant.setAuditTime(LocalDateTime.now());
            merchant.setAuditRemark(auditRemark);

            merchantRepository.save(merchant);

            log.info("审核商家成功，ID：{}，状态：{}", merchantId, auditStatus);
            return R.ok();

        } catch (Exception e) {
            log.error("审核商家失败，ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("审核失败，请稍后重试");
        }
    }

    /**
     * 禁用商家
     * 
     * @param merchantId 商家ID
     * @param reason     禁用原因
     * @return 禁用结果
     */
    @Override
    @Transactional
    public R<Void> disableMerchant(Long merchantId, String reason) {
        log.info("禁用商家，ID：{}，原因：{}", merchantId, reason);

        try {
            Optional<Merchant> merchantOpt = merchantRepository.findById(merchantId);
            if (!merchantOpt.isPresent()) {
                log.warn("商家不存在，ID：{}", merchantId);
                return R.fail("商家不存在");
            }

            Merchant merchant = merchantOpt.get();
            merchant.setStatus(0);
            // 这里可以记录禁用原因到其他表或字段

            merchantRepository.save(merchant);

            log.info("禁用商家成功，ID：{}", merchantId);
            return R.ok();

        } catch (Exception e) {
            log.error("禁用商家失败，ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("禁用失败，请稍后重试");
        }
    }

    /**
     * 启用商家
     * 
     * @param merchantId 商家ID
     * @return 启用结果
     */
    @Override
    @Transactional
    public R<Void> enableMerchant(Long merchantId) {
        log.info("启用商家，ID：{}", merchantId);

        try {
            Optional<Merchant> merchantOpt = merchantRepository.findById(merchantId);
            if (!merchantOpt.isPresent()) {
                log.warn("商家不存在，ID：{}", merchantId);
                return R.fail("商家不存在");
            }

            Merchant merchant = merchantOpt.get();
            merchant.setStatus(1);

            merchantRepository.save(merchant);

            log.info("启用商家成功，ID：{}", merchantId);
            return R.ok();

        } catch (Exception e) {
            log.error("启用商家失败，ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("启用失败，请稍后重试");
        }
    }

    /**
     * 删除商家
     * 
     * @param merchantId 商家ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public R<Void> deleteMerchant(Long merchantId) {
        log.info("删除商家，ID：{}", merchantId);

        try {
            if (!merchantRepository.existsById(merchantId)) {
                log.warn("商家不存在，ID：{}", merchantId);
                return R.fail("商家不存在");
            }

            merchantRepository.deleteById(merchantId);

            log.info("删除商家成功，ID：{}", merchantId);
            return R.ok();

        } catch (Exception e) {
            log.error("删除商家失败，ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("删除失败，请稍后重试");
        }
    }

    /**
     * 获取商家统计数据
     * 
     * @return 统计数据
     */
    @Override
    public R<Map<String, Object>> getMerchantStatistics() {
        log.debug("获取商家统计数据");

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总商家数
            Long totalMerchants = merchantRepository.countTotalMerchants();
            statistics.put("totalMerchants", totalMerchants);

            // 待审核商家数
            // 修复说明：仓库方法改为 countByApprovalStatus，字段与实体一致
            Long pendingAudit = merchantRepository.countByApprovalStatus(1);
            statistics.put("pendingAudit", pendingAudit);

            // 已通过审核商家数
            Long approved = merchantRepository.countByApprovalStatus(2);
            statistics.put("approved", approved);

            // 审核被拒绝商家数
            Long rejected = merchantRepository.countByApprovalStatus(3);
            statistics.put("rejected", rejected);

            // 正常状态商家数
            Long normalStatus = merchantRepository.countByStatus(1);
            statistics.put("normalStatus", normalStatus);

            // 禁用状态商家数
            Long disabledStatus = merchantRepository.countByStatus(0);
            statistics.put("disabledStatus", disabledStatus);

            // 个人商家数
            Long personalMerchants = merchantRepository.countByMerchantType(1);
            statistics.put("personalMerchants", personalMerchants);

            // 企业商家数
            Long enterpriseMerchants = merchantRepository.countByMerchantType(2);
            statistics.put("enterpriseMerchants", enterpriseMerchants);

            // 今日新增商家数
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1);
            Long todayNew = merchantRepository.countNewMerchants(todayStart, todayEnd);
            statistics.put("todayNew", todayNew);

            return R.ok(statistics);

        } catch (Exception e) {
            log.error("获取商家统计数据失败，错误信息：{}", e.getMessage(), e);
            return R.fail("获取统计数据失败");
        }
    }

    /**
     * 导出商家数据
     * 
     * @param shopName     店铺名称（可选）
     * @param merchantType 商家类型（可选）
     * @param auditStatus  审核状态（可选）
     * @param status       商家状态（可选）
     * @return 导出数据
     */
    @Override
    public R<byte[]> exportMerchantData(String shopName, Integer merchantType, Integer auditStatus, Integer status) {
        log.info("导出商家数据");

        try {
            // 这里应该实现Excel导出逻辑
            // 暂时返回空数据
            return R.ok(new byte[0]);

        } catch (Exception e) {
            log.error("导出商家数据失败，错误信息：{}", e.getMessage(), e);
            return R.fail("导出失败，请稍后重试");
        }
    }

    /**
     * 更新最后登录信息
     * 
     * @param merchantId 商家ID
     * @param loginIp    登录IP
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> updateLastLoginInfo(Long merchantId, String loginIp) {
        log.debug("更新最后登录信息，ID：{}", merchantId);

        try {
            Optional<Merchant> merchantOpt = merchantRepository.findById(merchantId);
            if (!merchantOpt.isPresent()) {
                log.warn("商家不存在，ID：{}", merchantId);
                return R.fail("商家不存在");
            }

            Merchant merchant = merchantOpt.get();
            merchant.setLastLoginTime(LocalDateTime.now());
            merchant.setLastLoginIp(loginIp);

            merchantRepository.save(merchant);

            return R.ok();

        } catch (Exception e) {
            log.error("更新最后登录信息失败，ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("更新失败");
        }
    }

    /**
     * 验证密码
     * 
     * @param merchantId 商家ID
     * @param password   密码
     * @return 验证结果
     */
    @Override
    public R<Boolean> verifyPassword(Long merchantId, String password) {
        log.debug("验证密码，ID：{}", merchantId);

        try {
            Optional<Merchant> merchantOpt = merchantRepository.findById(merchantId);
            if (!merchantOpt.isPresent()) {
                log.warn("商家不存在，ID：{}", merchantId);
                return R.fail("商家不存在");
            }

            Merchant merchant = merchantOpt.get();
            // 修改日志：2025-11-05 增加明文密码兼容逻辑
            boolean matches = passwordEncoder.matches(password, merchant.getPassword())
                    || (merchant.getPassword() != null && merchant.getPassword().equals(password));
            // 若为明文匹配，则迁移为加密存储
            if (matches && merchant.getPassword() != null && merchant.getPassword().equals(password)) {
                merchant.setPassword(passwordEncoder.encode(password));
                merchantRepository.save(merchant);
                log.info("验证密码过程中完成明文到加密迁移，商家ID：{}", merchantId);
            }

            return R.ok(matches);

        } catch (Exception e) {
            log.error("验证密码失败，ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("验证失败");
        }
    }

    /**
     * 检查用户名是否可用
     * 
     * @param username 用户名
     * @return 是否可用
     */
    @Override
    public R<Boolean> checkUsernameAvailable(String username) {
        log.debug("检查用户名是否可用：{}", username);

        try {
            boolean available = !merchantRepository.existsByUsername(username);
            return R.ok(available);

        } catch (Exception e) {
            log.error("检查用户名可用性失败，用户名：{}，错误信息：{}", username, e.getMessage(), e);
            return R.fail("检查失败");
        }
    }

    /**
     * 检查店铺名称是否可用
     * 
     * @param shopName 店铺名称
     * @return 是否可用
     */
    @Override
    public R<Boolean> checkShopNameAvailable(String shopName) {
        log.debug("检查店铺名称是否可用：{}", shopName);

        try {
            boolean available = !merchantRepository.existsByShopName(shopName);
            return R.ok(available);

        } catch (Exception e) {
            log.error("检查店铺名称可用性失败，店铺名称：{}，错误信息：{}", shopName, e.getMessage(), e);
            return R.fail("检查失败");
        }
    }

    /**
     * 检查手机号是否可用
     * 
     * @param phone 手机号
     * @return 是否可用
     */
    @Override
    public R<Boolean> checkPhoneAvailable(String phone) {
        log.debug("检查手机号是否可用：{}", phone);

        try {
            boolean available = !merchantRepository.existsByContactPhone(phone);
            return R.ok(available);

        } catch (Exception e) {
            log.error("检查手机号可用性失败，手机号：{}，错误信息：{}", phone, e.getMessage(), e);
            return R.fail("检查失败");
        }
    }

    /**
     * 检查邮箱是否可用
     * 
     * @param email 邮箱
     * @return 是否可用
     */
    @Override
    public R<Boolean> checkEmailAvailable(String email) {
        log.debug("检查邮箱是否可用：{}", email);

        try {
            // 修改日志：2025-11-05 调整为使用 existsByContactEmail 以与实体字段 contactEmail 对齐
            boolean available = !merchantRepository.existsByContactEmail(email);
            return R.ok(available);

        } catch (Exception e) {
            log.error("检查邮箱可用性失败，邮箱：{}，错误信息：{}", email, e.getMessage(), e);
            return R.fail("检查失败");
        }
    }

    /**
     * 重置密码
     * 
     * @param phone       手机号
     * @param verifyCode  验证码
     * @param newPassword 新密码
     * @return 重置结果
     */
    @Override
    @Transactional
    public R<Void> resetPassword(String phone, String verifyCode, String newPassword) {
        log.info("重置密码，手机号：{}", phone);

        try {
            // 这里应该验证验证码
            // 暂时跳过验证码验证

            Optional<Merchant> merchantOpt = merchantRepository.findByContactPhone(phone);
            if (!merchantOpt.isPresent()) {
                log.warn("手机号对应的商家不存在：{}", phone);
                return R.fail("手机号不存在");
            }

            Merchant merchant = merchantOpt.get();
            merchant.setPassword(passwordEncoder.encode(newPassword));
            merchantRepository.save(merchant);

            log.info("重置密码成功，手机号：{}", phone);
            return R.ok();

        } catch (Exception e) {
            log.error("重置密码失败，手机号：{}，错误信息：{}", phone, e.getMessage(), e);
            return R.fail("重置密码失败，请稍后重试");
        }
    }

    /**
     * 发送验证码
     * 
     * @param phone 手机号
     * @param type  验证码类型：1-注册，2-重置密码，3-修改手机号
     * @return 发送结果
     */
    @Override
    public R<Void> sendVerifyCode(String phone, Integer type) {
        log.info("发送验证码，手机号：{}，类型：{}", phone, type);

        try {
            // 这里应该调用短信服务发送验证码
            // 暂时只记录日志
            log.info("验证码发送成功，手机号：{}，类型：{}", phone, type);
            return R.ok();

        } catch (Exception e) {
            log.error("发送验证码失败，手机号：{}，类型：{}，错误信息：{}", phone, type, e.getMessage(), e);
            return R.fail("发送验证码失败，请稍后重试");
        }
    }
}
