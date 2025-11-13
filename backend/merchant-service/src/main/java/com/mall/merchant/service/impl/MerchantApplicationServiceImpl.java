package com.mall.merchant.service.impl;

import com.mall.merchant.domain.dto.MerchantApplicationDTO;
import com.mall.merchant.domain.entity.Merchant;
import com.mall.merchant.domain.entity.MerchantApplication;
import com.mall.merchant.domain.vo.MerchantApplicationVO;
import com.mall.merchant.repository.MerchantApplicationRepository;
import com.mall.merchant.repository.MerchantRepository;
import com.mall.merchant.service.MerchantApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家申请服务实现
 * 
 * @author system
 * @since 2025-11-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantApplicationServiceImpl implements MerchantApplicationService {

  private final MerchantApplicationRepository applicationRepository;
  private final MerchantRepository merchantRepository;
  private final PasswordEncoder passwordEncoder;
  private final RestTemplate restTemplate;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long submitApplication(MerchantApplicationDTO applicationDTO) {
    log.info("提交商家入驻申请 - shopName: {}, username: {}",
        applicationDTO.getShopName(), applicationDTO.getUsername());

    /*
     * 唯一性验证策略：
     * 1. 每个申请都有独立的自增 ID，保证全局唯一性
     * 2. 数据库层面无唯一约束，仅有普通索引优化查询
     * 3. 业务层面只验证待审批(0)和已通过(1)的记录
     * 4. 已拒绝(2)的申请允许重新提交，不限制次数
     * 5. 所有历史记录完整保留，支持完整的审计追溯
     */

    // 验证用户名：仅检查待审批(0)和已通过(1)的申请
    List<MerchantApplication> existingByUsername = applicationRepository
        .findAllByUsername(applicationDTO.getUsername());
    for (MerchantApplication existing : existingByUsername) {
      if (existing.getApprovalStatus() == 0) {
        throw new RuntimeException("该用户名已有待审批的申请，请等待审核结果");
      }
      if (existing.getApprovalStatus() == 1) {
        throw new RuntimeException("用户名已存在");
      }
      // 状态为2(已拒绝)时，允许重新提交，历史记录完整保留
    }

    // 验证手机号：仅检查待审批(0)和已通过(1)的申请
    List<MerchantApplication> existingByPhone = applicationRepository
        .findAllByContactPhone(applicationDTO.getContactPhone());
    for (MerchantApplication existing : existingByPhone) {
      if (existing.getApprovalStatus() == 0) {
        throw new RuntimeException("该手机号已有待审批的申请，请等待审核结果");
      }
      if (existing.getApprovalStatus() == 1) {
        throw new RuntimeException("手机号已被使用");
      }
    }

    // 验证店铺名称：仅检查待审批(0)和已通过(1)的申请
    List<MerchantApplication> existingByShopName = applicationRepository
        .findAllByShopName(applicationDTO.getShopName());
    for (MerchantApplication existing : existingByShopName) {
      if (existing.getApprovalStatus() == 0) {
        throw new RuntimeException("该店铺名称已有待审批的申请，请等待审核结果");
      }
      if (existing.getApprovalStatus() == 1) {
        throw new RuntimeException("店铺名称已存在");
      }
    }

    // 创建新申请实体（自动生成唯一ID，历史记录完整保留）
    MerchantApplication application = new MerchantApplication();
    BeanUtils.copyProperties(applicationDTO, application);

    // 加密密码
    application.setPassword(passwordEncoder.encode(applicationDTO.getPassword()));

    // 设置初始状态
    application.setApprovalStatus(0); // 待审批
    application.setSmsSent(false);
    application.setSmsRetryCount(0);

    // 保存到数据库
    MerchantApplication saved = applicationRepository.save(application);

    log.info("商家申请提交成功 - applicationId: {}, shopName: {}",
        saved.getId(), saved.getShopName());

    return saved.getId();
  }

  @Override
  public MerchantApplication getApplicationDetail(Long id) {
    return applicationRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("申请不存在"));
  }

  @Override
  public Page<MerchantApplicationVO> getApplicationList(Integer page, Integer size, Integer status, String keyword) {
    log.info("查询商家申请列表 - page: {}, size: {}, status: {}, keyword: {}",
        page, size, status, keyword);

    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));

    Page<MerchantApplication> applicationPage = applicationRepository.findByStatusAndKeyword(
        status, keyword, pageable);

    return applicationPage.map(this::convertToVO);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void auditApplication(Long id, Boolean approved, String reason, Long adminId, String adminName) {
    log.info("审核商家申请 - id: {}, approved: {}, adminId: {}", id, approved, adminId);

    MerchantApplication application = applicationRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("申请不存在"));

    if (application.getApprovalStatus() != 0) {
      throw new RuntimeException("该申请已被审核");
    }

    // 更新审核信息
    application.setApprovalStatus(approved ? 1 : 2);
    application.setApprovalReason(reason);
    application.setApprovalTime(LocalDateTime.now());
    application.setApprovalBy(adminId);
    application.setApprovalByName(adminName);

    // 如果审核通过，创建商家账号
    if (approved) {
      createMerchantAccount(application);
    }

    applicationRepository.save(application);

    // 发送短信通知（无论通过还是拒绝）
    sendApprovalSms(application, approved, reason);

    log.info("商家申请审核完成 - id: {}, approved: {}, 历史记录已完整保留", id, approved);
  }

  /**
   * 创建商家账号
   */
  private void createMerchantAccount(MerchantApplication application) {
    Merchant merchant = new Merchant();

    merchant.setUsername(application.getUsername());
    merchant.setPassword(application.getPassword()); // 已加密
    merchant.setShopName(application.getShopName());
    merchant.setContactName(application.getContactName());
    merchant.setContactPhone(application.getContactPhone());
    merchant.setContactEmail(application.getEmail());

    // 根据主体类型设置商家类型和公司信息
    if ("enterprise".equals(application.getEntityType()) || "individual".equals(application.getEntityType())) {
      merchant.setMerchantType(2); // 企业商家
      merchant.setCompanyName(
          application.getCompanyName() != null ? application.getCompanyName() : application.getShopName());
      merchant.setIdNumber(application.getCreditCode());
      merchant.setIdFrontImage(application.getBusinessLicense());
    } else {
      merchant.setMerchantType(1); // 个人商家
      merchant.setCompanyName(application.getShopName()); // 个人商家使用店铺名称
      merchant.setIdNumber(application.getIdCard());
      merchant.setIdFrontImage(application.getIdCardFront());
      merchant.setIdBackImage(application.getIdCardBack());
    }

    // 设置默认值
    merchant.setRealName(application.getContactName());
    merchant.setStatus(1); // 正常状态
    merchant.setApprovalStatus(1); // 已通过审批

    // 生成临时编码（使用时间戳+随机数，保存后用ID替换）
    String tempCode = "TEMP" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    merchant.setMerchantCode(tempCode);

    // 保存获取ID
    Merchant savedMerchant = merchantRepository.save(merchant);

    // 生成正式商家编码：MER + 商家ID + 时间戳后6位
    String merchantCode = "MER" + savedMerchant.getId() + String.format("%06d", System.currentTimeMillis() % 1000000);
    savedMerchant.setMerchantCode(merchantCode);

    // 更新商家编码
    merchantRepository.save(savedMerchant);

    // 更新申请中的商家ID
    application.setMerchantId(savedMerchant.getId());

    log.info("商家账号创建成功 - merchantId: {}, merchantCode: {}, companyName: {}, username: {}",
        savedMerchant.getId(), merchantCode, merchant.getCompanyName(), savedMerchant.getUsername());
  }

  @Override
  public Map<String, Object> getApplicationStats() {
    Map<String, Object> stats = new HashMap<>();

    // 统计各状态的申请数量
    long pending = applicationRepository.countByApprovalStatus(0);
    long approved = applicationRepository.countByApprovalStatus(1);
    long rejected = applicationRepository.countByApprovalStatus(2);
    long total = applicationRepository.count();

    stats.put("pending", pending);
    stats.put("approved", approved);
    stats.put("rejected", rejected);
    stats.put("total", total);

    return stats;
  }

  /**
   * 转换为VO
   */
  private MerchantApplicationVO convertToVO(MerchantApplication application) {
    MerchantApplicationVO vo = new MerchantApplicationVO();
    BeanUtils.copyProperties(application, vo);

    // 确保时间字段正确映射
    vo.setCreatedTime(application.getCreatedTime());
    vo.setCreatedAt(application.getCreatedTime()); // 兼容字段
    vo.setUpdatedAt(application.getUpdatedTime());

    // 添加文本字段
    vo.setEntityTypeText(getEntityTypeText(application.getEntityType()));
    vo.setShopTypeText(getShopTypeText(application.getShopType()));

    // 脱敏手机号（用于列表显示）
    if (application.getContactPhone() != null && application.getContactPhone().length() >= 11) {
      String phone = application.getContactPhone();
      vo.setContactPhoneMasked(phone.substring(0, 3) + "****" + phone.substring(7));
    }

    return vo;
  }

  /**
   * 发送审批短信通知
   * 使用异步线程发送，避免阻塞主业务流程
   */
  private void sendApprovalSms(MerchantApplication application, Boolean approved, String reason) {
    try {
      final Long applicationId = application.getId();
      final String phone = application.getContactPhone();
      final String shopName = application.getShopName();
      final String username = application.getUsername();

      Map<String, Object> smsData = new HashMap<>();
      smsData.put("phoneNumber", phone);

      if (approved) {
        // 通过通知
        String message = String.format(
            "【在线商城】恭喜！您的商家入驻申请已审核通过！店铺名称：%s，登录账号：%s，请访问商家后台开启电商之旅！",
            shopName, username);
        smsData.put("purpose", "MERCHANT_APPROVAL_PASS");
        smsData.put("message", message);

        log.info("准备发送审批通过短信 - 申请ID: {}, 手机: {}, 店铺: {}", applicationId, phone, shopName);
      } else {
        // 拒绝通知
        String message = String.format(
            "【在线商城】很遗憾，您的商家入驻申请未通过审核。店铺名称：%s，拒绝原因：%s",
            shopName, reason != null && !reason.isEmpty() ? reason : "未提供");
        smsData.put("purpose", "MERCHANT_APPROVAL_REJECT");
        smsData.put("message", message);

        log.info("准备发送审批拒绝短信 - 申请ID: {}, 手机: {}, 店铺: {}, 原因: {}",
            applicationId, phone, shopName, reason);
      }

      // 异步发送短信（不阻塞主流程）
      // 使用独立线程避免事务边界问题
      new Thread(() -> {
        try {
          // 等待短暂时间，确保主事务已提交
          Thread.sleep(500);

          String smsUrl = "http://sms-service/sms/send";
          log.info("开始调用SMS服务 - URL: {}, 申请ID: {}, 手机: {}", smsUrl, applicationId, phone);

          ResponseEntity<Map> response = restTemplate.postForEntity(smsUrl, smsData, Map.class);

          if (response.getStatusCode().is2xxSuccessful()) {
            log.info("审批短信发送成功 - 申请ID: {}, 手机: {}, 结果: {}, 响应: {}",
                applicationId, phone, approved ? "通过" : "拒绝", response.getBody());

            // 更新短信发送状态（使用新事务）
            try {
              MerchantApplication app = applicationRepository.findById(applicationId).orElse(null);
              if (app != null) {
                app.setSmsSent(true);
                app.setSmsSentTime(LocalDateTime.now());
                applicationRepository.save(app);
                log.info("短信发送状态已更新 - 申请ID: {}", applicationId);
              }
            } catch (Exception e) {
              log.error("更新短信发送状态失败 - 申请ID: {}", applicationId, e);
            }
          } else {
            log.error("审批短信发送失败 - 申请ID: {}, 手机: {}, 状态码: {}, 响应体: {}",
                applicationId, phone, response.getStatusCode(), response.getBody());
          }
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          log.error("发送审批短信线程被中断 - 申请ID: {}, 手机: {}", applicationId, phone);
        } catch (Exception e) {
          log.error("发送审批短信异常 - 申请ID: {}, 手机: {}", applicationId, phone, e);
        }
      }, "SMS-Approval-Thread-" + applicationId).start();

    } catch (Exception e) {
      log.error("准备发送审批短信失败 - 申请ID: {}", application.getId(), e);
      // 不抛出异常，避免影响审批流程
    }
  }

  /**
   * 获取主体类型文本
   */
  private String getEntityTypeText(String entityType) {
    if (entityType == null)
      return "";
    switch (entityType) {
      case "enterprise":
        return "企业";
      case "individual":
        return "个体工商户";
      case "personal":
        return "个人";
      default:
        return entityType;
    }
  }

  /**
   * 获取店铺类型文本
   */
  private String getShopTypeText(String shopType) {
    if (shopType == null)
      return "";
    switch (shopType) {
      case "flagship":
        return "旗舰店";
      case "specialty":
        return "专卖店";
      case "franchise":
        return "专营店";
      case "ordinary":
        return "普通企业店";
      case "small":
        return "小店";
      default:
        return shopType;
    }
  }
}
