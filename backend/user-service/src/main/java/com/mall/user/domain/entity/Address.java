package com.mall.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收货地址实体类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@TableName("user_addresses")
public class Address implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 地址ID */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /** 用户ID */
  private Long userId;

  /** 收货人姓名 */
  private String receiverName;

  /** 收货人手机号 */
  private String receiverPhone;

  /** 省份代码 */
  private String provinceCode;

  /** 城市代码 */
  private String cityCode;

  /** 区县代码 */
  private String districtCode;

  /** 省份名称 */
  private String province;

  /** 城市名称 */
  private String city;

  /** 区县名称 */
  private String district;

  /** 详细地址 */
  private String detailAddress;

  /** 是否默认地址：0-否，1-是 */
  private Boolean isDefault;

  /** 创建时间 */
  @TableField("created_time")
  private LocalDateTime createdTime;

  /** 更新时间 */
  @TableField("updated_time")
  private LocalDateTime updatedTime;
}
