package com.mall.common.core.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类 - 暂时禁用MyBatis Plus注解
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
/**
 * JPA 基础实体父类
 * 作为所有 JPA 实体的父类，统一提供主键、审计字段等。
 * 说明：
 * - 使用 @MappedSuperclass 让子类继承映射字段；
 * - 使用 @Id + 主键生成策略，保证每个实体都有主键标识；
 * - 保留原有（注释掉的）MyBatis Plus 注解以兼容历史（不删除旧注释）。
 *
 * @author lingbai
 */
@Data
@MappedSuperclass
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    // @TableId(type = IdType.ASSIGN_ID)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** 创建时间 */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建者 */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /** 更新者 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /** 删除标志（0代表存在 1代表删除） */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /** 版本号（乐观锁） */
    // @TableField(fill = FieldFill.INSERT)
    private Integer version;
}