package com.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.product.domain.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商品数据访问层接口
 * 继承 MyBatis-Plus BaseMapper，提供基础 CRUD 操作
 * 同时提供自定义的业务查询方法
 * 
 * @author lingbai
 * @version 2.0
 * @since 2025-10-22
 * 修改日志：V2.0 2025-12-01：继承 BaseMapper，添加商家筛选支持
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 根据商家ID查询商品列表
     * 
     * @param merchantId 商家ID
     * @return 商品列表
     */
    @Select("SELECT * FROM products WHERE merchant_id = #{merchantId} AND deleted = 0")
    List<Product> selectByMerchantId(@Param("merchantId") Long merchantId);
    
    /**
     * 根据商家ID统计商品数量
     * 
     * @param merchantId 商家ID（可选）
     * @param status 状态（可选）
     * @return 商品数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM products WHERE deleted = 0 " +
            "<if test='merchantId != null'> AND merchant_id = #{merchantId}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "</script>")
    Long countByCondition(@Param("merchantId") Long merchantId, @Param("status") Integer status);
    
    /**
     * 查询库存预警商品
     * 
     * @param merchantId 商家ID（可选，为空则查询全部）
     * @return 库存预警商品列表
     */
    @Select("<script>" +
            "SELECT * FROM products WHERE deleted = 0 AND stock &lt;= stock_warning " +
            "<if test='merchantId != null'> AND merchant_id = #{merchantId}</if>" +
            " ORDER BY stock ASC" +
            "</script>")
    List<Product> selectStockWarningProducts(@Param("merchantId") Long merchantId);
    
    /**
     * 查询热销商品
     * 
     * @param merchantId 商家ID（可选）
     * @param limit 限制数量
     * @return 热销商品列表
     */
    @Select("<script>" +
            "SELECT * FROM products WHERE deleted = 0 AND status = 1 " +
            "<if test='merchantId != null'> AND merchant_id = #{merchantId}</if>" +
            " ORDER BY sales DESC LIMIT #{limit}" +
            "</script>")
    List<Product> selectHotProducts(@Param("merchantId") Long merchantId, @Param("limit") Integer limit);
    
    /**
     * 扣减库存（带乐观锁）
     * 
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 影响行数
     */
    @Update("UPDATE products SET stock = stock - #{quantity}, updated_time = NOW() " +
            "WHERE id = #{productId} AND stock >= #{quantity} AND deleted = 0")
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 恢复库存
     * 
     * @param productId 商品ID
     * @param quantity 恢复数量
     * @return 影响行数
     */
    @Update("UPDATE products SET stock = stock + #{quantity}, updated_time = NOW() " +
            "WHERE id = #{productId} AND deleted = 0")
    int restoreStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 增加销量
     * 
     * @param productId 商品ID
     * @param quantity 增加数量
     * @return 影响行数
     */
    @Update("UPDATE products SET sales = sales + #{quantity}, updated_time = NOW() " +
            "WHERE id = #{productId} AND deleted = 0")
    int increaseSales(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 计算商家销售统计
     * 
     * @param merchantId 商家ID（可选）
     * @return 总销量
     */
    @Select("<script>" +
            "SELECT COALESCE(SUM(sales), 0) FROM products WHERE deleted = 0 " +
            "<if test='merchantId != null'> AND merchant_id = #{merchantId}</if>" +
            "</script>")
    Long sumSales(@Param("merchantId") Long merchantId);
    
    /**
     * 计算商家销售额
     * 
     * @param merchantId 商家ID（可选）
     * @return 总销售额
     */
    @Select("<script>" +
            "SELECT COALESCE(SUM(sales * price), 0) FROM products WHERE deleted = 0 " +
            "<if test='merchantId != null'> AND merchant_id = #{merchantId}</if>" +
            "</script>")
    Double sumRevenue(@Param("merchantId") Long merchantId);
}
