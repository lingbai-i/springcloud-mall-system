package com.mall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.user.domain.entity.UserFavorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户收藏数据访问层
 * 
 * @author lingbai
 * @version 1.0
 * P25-09-20
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
    
    /**
     * 根据用户ID和商品ID查询收藏记录
     */
    @Select("SELECT * FROM user_favorites WHERE user_id = #{userId} AND product_id = #{productId} AND deleted = 0")
    UserFavorite findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    
    /**
     * 检查是否已收藏
     */
    @Select("SELECT COUNT(*) > 0 FROM user_favorites WHERE user_id = #{userId} AND product_id = #{productId} AND deleted = 0")
    boolean existsByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    
    /**
     * 根据用户ID分页查询收藏列表（按创建时间倒序）
     */
    @Select("SELECT * FROM user_favorites WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_time DESC")
    IPage<UserFavorite> findByUserIdOrderByCreateTimeDesc(Page<UserFavorite> page, @Param("userId") Long userId);
    
    /**
     * 删除收藏记录（物理删除）
     */
    @Delete("DELETE FROM user_favorites WHERE user_id = #{userId} AND product_id = #{productId}")
    int deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    
    /**
     * 批量删除收藏记录
     */
    @Delete("<script>" +
            "DELETE FROM user_favorites WHERE user_id = #{userId} AND product_id IN " +
            "<foreach collection='productIds' item='productId' open='(' separator=',' close=')'>" +
            "#{productId}" +
            "</foreach>" +
            "</script>")
    int deleteByUserIdAndProductIdIn(@Param("userId") Long userId, @Param("productIds") List<Long> productIds);
    
    /**
     * 统计用户收藏数量
     */
    @Select("SELECT COUNT(*) FROM user_favorites WHERE user_id = #{userId} AND deleted = 0")
    long countByUserId(@Param("userId") Long userId);
}
