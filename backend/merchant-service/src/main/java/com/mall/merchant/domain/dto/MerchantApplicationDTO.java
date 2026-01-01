package com.mall.merchant.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家入驻申请DTO
 * 
 * @author lingbai
 * @since 2025-11-12
 */
@Data
public class MerchantApplicationDTO {

  /**
   * 主体类型：enterprise-企业, individual-个体, personal-个人
   */
  @NotBlank(message = "主体类型不能为空")
  private String entityType;

  /**
   * 店铺类型：flagship-旗舰店, specialty-专卖店, franchise-专营店, ordinary-普通企业店, small-小店
   */
  private String shopType;

  /**
   * 店铺名称
   */
  @NotBlank(message = "店铺名称不能为空")
  @Size(min = 2, max = 50, message = "店铺名称长度在2到50个字符之间")
  private String shopName;

  /**
   * 联系人姓名
   */
  @NotBlank(message = "联系人姓名不能为空")
  private String contactName;

  /**
   * 联系电话
   */
  @NotBlank(message = "联系电话不能为空")
  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号码")
  private String contactPhone;

  /**
   * 邮箱地址
   */
  @NotBlank(message = "邮箱地址不能为空")
  @Email(message = "请输入正确的邮箱地址")
  private String email;

  /**
   * 公司名称（企业/个体户）
   */
  private String companyName;

  /**
   * 统一社会信用代码（企业/个体户）
   */
  private String creditCode;

  /**
   * 法人代表（企业/个体户）
   */
  private String legalPerson;

  /**
   * 营业执照图片URL（企业/个体户）
   */
  private String businessLicense;

  /**
   * 身份证号（个人）
   */
  private String idCard;

  /**
   * 身份证正面照片URL（个人）
   */
  private String idCardFront;

  /**
   * 身份证反面照片URL（个人）
   */
  private String idCardBack;

  /**
   * 登录账号
   */
  @NotBlank(message = "登录账号不能为空")
  @Size(min = 6, max = 20, message = "账号长度在6到20个字符之间")
  @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "账号只能包含字母、数字和下划线")
  private String username;

  /**
   * 登录密码
   */
  @NotBlank(message = "登录密码不能为空")
  @Size(min = 6, max = 20, message = "密码长度在6到20个字符之间")
  private String password;
}
