package com.mall.product.domain.entity;

// import com.baomidou.mybatisplus.annotation.TableField;
// import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商品分类实体类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
// @TableName("t_category")
public class Category extends BaseEntity {
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 父分类ID，0表示顶级分类
     */
    private Long parentId;
    
    /**
     * 分类层级：1-一级分类，2-二级分类，3-三级分类
     */
    private Integer level;
    
    /**
     * 分类排序
     */
    private Integer sort;
    
    /**
     * 分类图标URL
     */
    private String icon;
    
    /**
     * 分类状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 子分类列表（用于构建分类树，不存储到数据库）
     */
    // @TableField(exist = false)
    private List<Category> children;
}