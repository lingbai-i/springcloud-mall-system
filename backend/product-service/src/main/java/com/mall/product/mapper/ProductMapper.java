package com.mall.product.mapper;

import com.mall.product.domain.entity.Product;
// import org.apache.ibatis.annotations.Mapper;
// import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品数据访问层接口
 * 提供商品相关的数据库操作方法，包括基本的CRUD操作和业务查询
 * 支持分页查询、搜索、库存管理等功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-10-22
 */
// @Mapper  // 暂时注释掉，避免在没有数据源配置时出错
public interface ProductMapper {
    
    /**
     * 查询所有商品
     * 
     * @return 商品列表
     */
    List<Product> selectAll();

    /**
     * 分页查询商品
     * 
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 商品列表
     */
    List<Product> selectByPage(/* @Param("offset") */ Long offset, /* @Param("limit") */ Long limit);

    /**
     * 统计商品总数
     * 
     * @return 商品总数
     */
    Long countAll();

    /**
     * 根据分类ID分页查询商品
     * 
     * @param offset 偏移量
     * @param limit 限制数量
     * @param categoryId 分类ID
     * @param status 商品状态
     * @return 商品列表
     */
    List<Product> selectPageByCategoryId(/* @Param("offset") */ Long offset, 
                                        /* @Param("limit") */ Long limit,
                                        /* @Param("categoryId") */ Long categoryId,
                                        /* @Param("status") */ Integer status);

    /**
     * 根据分类ID统计商品数量
     * 
     * @param categoryId 分类ID
     * @param status 商品状态
     * @return 商品数量
     */
    Long countByCategoryId(/* @Param("categoryId") */ Long categoryId, /* @Param("status") */ Integer status);
    
    /**
     * 搜索商品
     * 
     * @param offset 偏移量
     * @param limit 限制数量
     * @param keyword 搜索关键词
     * @param status 商品状态
     * @return 商品列表
     */
    List<Product> searchProducts(/* @Param("offset") */ Long offset,
                                /* @Param("limit") */ Long limit,
                                /* @Param("keyword") */ String keyword,
                                /* @Param("status") */ Integer status);

    /**
     * 统计搜索结果数量
     * 
     * @param keyword 搜索关键词
     * @param status 商品状态
     * @return 搜索结果数量
     */
    Long countSearchResults(/* @Param("keyword") */ String keyword, /* @Param("status") */ Integer status);
    
    /**
     * 查询热门商品
     * 
     * @param limit 限制数量
     * @return 热门商品列表
     */
    List<Product> selectHotProducts(/* @Param("limit") */ Integer limit);

    /**
     * 查询推荐商品
     * 
     * @param limit 限制数量
     * @return 推荐商品列表
     */
    List<Product> selectRecommendProducts(/* @Param("limit") */ Integer limit);

    /**
     * 根据ID查询商品
     * 
     * @param id 商品ID
     * @return 商品信息
     */
    Product selectById(/* @Param("id") */ Long id);

    /**
     * 新增商品
     * 
     * @param product 商品对象
     * @return 影响行数
     */
    int insert(Product product);

    /**
     * 更新商品
     * 
     * @param product 商品对象，必须包含ID
     * @return 影响行数
     */
    int update(Product product);

    /**
     * 根据ID删除商品
     * 
     * @param id 商品ID
     * @return 影响行数
     */
    int deleteById(/* @Param("id") */ Long id);

    /**
     * 批量删除商品
     * 
     * @param ids 商品ID列表
     * @return 影响行数
     */
    int deleteBatchByIds(/* @Param("ids") */ List<Long> ids);
    
    /**
     * 更新商品库存
     * 
     * @param productId 商品ID
     * @param quantity 库存变化数量（正数为增加，负数为减少）
     * @return 影响行数
     */
    int updateStock(/* @Param("productId") */ Long productId, 
                   /* @Param("quantity") */ Integer quantity);

    /**
     * 设置商品库存
     * 
     * @param productId 商品ID
     * @param newStock 新的库存数量
     * @return 影响行数
     */
    int updateStockById(/* @Param("productId") */ Long productId, /* @Param("newStock") */ Integer newStock);

    /**
     * 更新商品状态
     * 
     * @param id 商品ID
     * @param status 状态值（1-上架，0-下架）
     * @return 影响行数
     */
    int updateStatus(/* @Param("id") */ Long id, /* @Param("status") */ Integer status);

    /**
     * 根据状态查询商品
     * 
     * @param status 状态值（1-上架，0-下架）
     * @return 商品列表
     */
    List<Product> selectByStatus(/* @Param("status") */ Integer status);

    /**
     * 检查商品名称是否存在
     * 
     * @param name 商品名称
     * @param excludeId 排除的ID（修改时排除自身）
     * @return 存在数量
     */
    int checkNameExists(/* @Param("name") */ String name, /* @Param("excludeId") */ Long excludeId);
}