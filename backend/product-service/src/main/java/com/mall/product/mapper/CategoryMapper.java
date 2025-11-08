package com.mall.product.mapper;

import com.mall.product.domain.entity.Category;
// import org.apache.ibatis.annotations.Mapper;
// import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类数据访问层接口
 * 提供分类相关的数据库操作方法，包括基本的CRUD操作和业务查询
 * 支持分页查询、层级查询、状态管理等功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-10-22
 */
// @Mapper  // 暂时注释掉，避免在没有数据源配置时出错
public interface CategoryMapper {

    /**
     * 查询所有分类
     * 
     * @return 分类列表
     */
    List<Category> selectAll();

    /**
     * 分页查询分类
     * 
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 分类列表
     */
    List<Category> selectByPage(/* @Param("offset") */ Long offset, /* @Param("limit") */ Long limit);

    /**
     * 统计分类总数
     * 
     * @return 分类总数
     */
    Long countAll();

    /**
     * 根据ID查询分类
     * 
     * @param id 分类ID
     * @return 分类信息
     */
    Category selectById(/* @Param("id") */ Long id);

    /**
     * 根据父级ID查询子分类
     * 
     * @param parentId 父级分类ID
     * @return 子分类列表
     */
    List<Category> selectByParentId(/* @Param("parentId") */ Long parentId);

    /**
     * 查询根分类（顶级分类）
     * 
     * @return 根分类列表
     */
    List<Category> selectRootCategories();

    /**
     * 新增分类
     * 
     * @param category 分类对象
     * @return 影响行数
     */
    int insert(Category category);

    /**
     * 更新分类
     * 
     * @param category 分类对象，必须包含ID
     * @return 影响行数
     */
    int update(Category category);

    /**
     * 根据ID删除分类
     * 
     * @param id 分类ID
     * @return 影响行数
     */
    int deleteById(/* @Param("id") */ Long id);

    /**
     * 批量删除分类
     * 
     * @param ids 分类ID列表
     * @return 影响行数
     */
    int deleteBatchByIds(/* @Param("ids") */ List<Long> ids);

    /**
     * 更新分类状态
     * 
     * @param id 分类ID
     * @param status 状态值（1-启用，0-禁用）
     * @return 影响行数
     */
    int updateStatus(/* @Param("id") */ Long id, /* @Param("status") */ Integer status);

    /**
     * 检查分类名称是否存在
     * 
     * @param name 分类名称
     * @param excludeId 排除的ID（修改时排除自身）
     * @return 存在数量
     */
    int checkNameExists(/* @Param("name") */ String name, /* @Param("excludeId") */ Long excludeId);

    /**
     * 统计指定父级分类下的子分类数量
     * 
     * @param parentId 父级分类ID
     * @return 子分类数量
     */
    int countByParentId(/* @Param("parentId") */ Long parentId);

    /**
     * 根据状态查询分类
     * 
     * @param status 状态值（1-启用，0-禁用）
     * @return 分类列表
     */
    List<Category> selectByStatus(/* @Param("status") */ Integer status);

    /**
     * 查询分类路径（从根分类到指定分类的完整路径）
     * 
     * @param id 分类ID
     * @return 分类路径列表
     */
    List<Category> selectCategoryPath(/* @Param("id") */ Long id);
}