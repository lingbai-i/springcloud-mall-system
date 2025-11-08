package com.mall.merchant.controller;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.merchant.domain.entity.MerchantProduct;
import com.mall.merchant.service.MerchantProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商家商品管理控制器
 * 提供商品的增删改查、上下架、库存管理等功能的REST API接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@RestController
@RequestMapping("/api/merchant/product")
@RequiredArgsConstructor
@Validated
@Tag(name = "商家商品管理", description = "商品的增删改查、上下架、库存管理等功能")
public class MerchantProductController {
    
    private static final Logger log = LoggerFactory.getLogger(MerchantProductController.class);
    
    private final MerchantProductService productService;
    
    /**
     * 新增商品
     * 商家发布新商品
     * 
     * @param product 商品信息
     * @return 新增结果
     */
    @PostMapping
    @Operation(summary = "新增商品", description = "商家发布新商品")
    public R<Void> addProduct(@Valid @RequestBody MerchantProduct product) {
        log.info("新增商品请求，商家ID：{}，商品名称：{}", product.getMerchantId(), product.getProductName());
        return productService.addProduct(product.getMerchantId(), product);
    }
    
    /**
     * 更新商品信息
     * 修改商品的基本信息
     * 
     * @param productId 商品ID
     * @param product 更新的商品信息
     * @return 更新结果
     */
    @PutMapping("/{productId}")
    @Operation(summary = "更新商品信息", description = "修改商品的基本信息")
    public R<Void> updateProduct(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long productId,
            @Valid @RequestBody MerchantProduct product) {
        log.info("更新商品信息请求，商品ID：{}，商家ID：{}", productId, product.getMerchantId());
        product.setId(productId);
        return productService.updateProduct(product.getMerchantId(), product);
    }
    
    /**
     * 删除商品
     * 删除指定商品
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @return 删除结果
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "删除商品", description = "删除指定商品")
    public R<Void> deleteProduct(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long productId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.info("删除商品请求，商品ID：{}，商家ID：{}", productId, merchantId);
        return productService.deleteProduct(merchantId, productId);
    }
    
    /**
     * 获取商品详情
     * 根据商品ID获取详细信息
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID（可选，用于权限验证）
     * @return 商品详情
     */
    @GetMapping("/{productId}")
    @Operation(summary = "获取商品详情", description = "根据商品ID获取详细信息")
    public R<MerchantProduct> getProductById(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long productId,
            @Parameter(description = "商家ID") @RequestParam(required = false) Long merchantId) {
        log.debug("获取商品详情请求，商品ID：{}，商家ID：{}", productId, merchantId);
        return productService.getProductById(productId, merchantId);
    }
    
    /**
     * 分页查询商品列表
     * 根据条件分页查询商家的商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @param keyword 关键词（可选）
     * @param categoryId 分类ID（可选）
     * @param brandId 品牌ID（可选）
     * @param status 商品状态（可选）
     * @param minPrice 最低价格（可选）
     * @param maxPrice 最高价格（可选）
     * @return 商品分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询商品列表", description = "根据条件分页查询商家的商品列表")
    public R<PageResult<MerchantProduct>> getProductList(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "品牌") @RequestParam(required = false) String brand,
            @Parameter(description = "商品状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "最低价格") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "最高价格") @RequestParam(required = false) BigDecimal maxPrice) {
        log.debug("分页查询商品列表请求，商家ID：{}，页码：{}，大小：{}", merchantId, page, size);
        // 同步接口签名：品牌字段使用字符串
        return productService.getProductList(merchantId, page, size, keyword, categoryId, brand, status, minPrice, maxPrice);
    }
    
    /**
     * 商品上架
     * 将商品状态设置为上架
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @return 上架结果
     */
    @PostMapping("/{productId}/on-shelf")
    @Operation(summary = "商品上架", description = "将商品状态设置为上架")
    public R<Void> putProductOnShelf(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long productId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.info("商品上架请求，商品ID：{}，商家ID：{}", productId, merchantId);
        return productService.onlineProduct(merchantId, productId);
    }
    
    /**
     * 商品下架
     * 将商品状态设置为下架
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @return 下架结果
     */
    @PostMapping("/{productId}/off-shelf")
    @Operation(summary = "商品下架", description = "将商品状态设置为下架")
    public R<Void> putProductOffShelf(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long productId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.info("商品下架请求，商品ID：{}，商家ID：{}", productId, merchantId);
        return productService.offlineProduct(merchantId, productId);
    }
    
    /**
     * 批量上架商品
     * 批量将多个商品设置为上架状态
     * 
     * @param merchantId 商家ID
     * @param productIds 商品ID列表
     * @return 批量上架结果
     */
    @PostMapping("/batch-on-shelf")
    @Operation(summary = "批量上架商品", description = "批量将多个商品设置为上架状态")
    public R<Void> batchPutProductsOnShelf(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @RequestBody List<Long> productIds) {
        log.info("批量上架商品请求，商家ID：{}，商品数量：{}", merchantId, productIds.size());
        return productService.batchOnlineProducts(merchantId, productIds);
    }
    
    /**
     * 批量下架商品
     * 批量将多个商品设置为下架状态
     * 
     * @param merchantId 商家ID
     * @param productIds 商品ID列表
     * @return 批量下架结果
     */
    @PostMapping("/batch-off-shelf")
    @Operation(summary = "批量下架商品", description = "批量将多个商品设置为下架状态")
    public R<Void> batchPutProductsOffShelf(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @RequestBody List<Long> productIds) {
        log.info("批量下架商品请求，商家ID：{}，商品数量：{}", merchantId, productIds.size());
        return productService.batchOfflineProducts(merchantId, productIds);
    }
    
    /**
     * 批量删除商品
     * 批量删除多个商品
     * 
     * @param merchantId 商家ID
     * @param productIds 商品ID列表
     * @return 批量删除结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除商品", description = "批量删除多个商品")
    public R<Void> batchDeleteProducts(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @RequestBody List<Long> productIds) {
        log.info("批量删除商品请求，商家ID：{}，商品数量：{}", merchantId, productIds.size());
        return productService.batchDeleteProducts(merchantId, productIds);
    }
    
    /**
     * 更新商品库存
     * 修改商品的库存数量
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @param quantity 库存变化量（正数增加，负数减少）
     * @return 更新结果
     */
    @PutMapping("/{productId}/stock")
    @Operation(summary = "更新商品库存", description = "修改商品的库存数量")
    public R<Void> updateProductStock(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long productId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "库存变化量") @RequestParam @NotNull Integer quantity) {
        log.info("更新商品库存请求，商品ID：{}，商家ID：{}，库存变化量：{}", productId, merchantId, quantity);
        return productService.updateStock(merchantId, productId, quantity);
    }
    
    /**
     * 更新商品价格
     * 修改商品的销售价格
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @param price 新价格
     * @return 更新结果
     */
    @PutMapping("/{productId}/price")
    @Operation(summary = "更新商品价格", description = "修改商品的销售价格")
    public R<Void> updateProductPrice(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long productId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "新价格") @RequestParam @NotNull BigDecimal price) {
        log.info("更新商品价格请求，商品ID：{}，商家ID：{}，价格：{}", productId, merchantId, price);
        return productService.updatePrice(merchantId, productId, price);
    }
    
    /**
     * 设置推荐商品
     * 将商品设置为推荐状态
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @param isRecommended 是否推荐
     * @return 设置结果
     */
    @PutMapping("/{productId}/recommend")
    @Operation(summary = "设置推荐商品", description = "将商品设置为推荐状态")
    public R<Void> setProductRecommended(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long productId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "是否推荐") @RequestParam @NotNull Boolean isRecommended) {
        log.info("设置推荐商品请求，商品ID：{}，商家ID：{}，是否推荐：{}", productId, merchantId, isRecommended);
        return productService.setRecommendStatus(merchantId, productId, isRecommended);
    }
    
    /**
     * 设置新品商品
     * 将商品设置为新品状态
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @param isNew 是否新品
     * @return 设置结果
     */
    @PutMapping("/{productId}/new")
    @Operation(summary = "设置新品商品", description = "将商品设置为新品状态")
    public R<Void> setProductNew(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long productId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "是否新品") @RequestParam @NotNull Boolean isNew) {
        log.info("设置新品商品请求，商品ID：{}，商家ID：{}，是否新品：{}", productId, merchantId, isNew);
        return productService.setNewStatus(merchantId, productId, isNew);
    }
    
    /**
     * 设置热销商品
     * 将商品设置为热销状态
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @param isHot 是否热销
     * @return 设置结果
     */
    @PutMapping("/{productId}/hot")
    @Operation(summary = "设置热销商品", description = "将商品设置为热销状态")
    public R<Void> setProductHot(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long productId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "是否热销") @RequestParam @NotNull Boolean isHot) {
        log.info("设置热销商品请求，商品ID：{}，商家ID：{}，是否热销：{}", productId, merchantId, isHot);
        return productService.setHotStatus(merchantId, productId, isHot);
    }
    
    /**
     * 增加商品浏览量
     * 用户浏览商品时调用，增加浏览计数
     * 
     * @param productId 商品ID
     * @return 增加结果
     */
    @PostMapping("/{productId}/view")
    @Operation(summary = "增加商品浏览量", description = "用户浏览商品时调用")
    public R<Void> increaseProductViewCount(@Parameter(description = "商品ID") @PathVariable @NotNull Long productId) {
        log.debug("增加商品浏览量请求，商品ID：{}", productId);
        return productService.increaseViewCount(productId);
    }
    
    /**
     * 增加商品收藏数
     * 用户收藏商品时调用，增加收藏计数
     * 
     * @param productId 商品ID
     * @return 增加结果
     */
    @PostMapping("/{productId}/favorite")
    @Operation(summary = "增加商品收藏数", description = "用户收藏商品时调用")
    public R<Void> increaseProductFavoriteCount(@Parameter(description = "商品ID") @PathVariable @NotNull Long productId) {
        log.debug("增加商品收藏数请求，商品ID：{}", productId);
        return productService.increaseFavoriteCount(productId);
    }
    
    /**
     * 增加商品销售数量
     * 订单完成时调用，增加销售计数
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @param quantity 销售数量
     * @return 增加结果
     */
    @PostMapping("/{productId}/sale")
    @Operation(summary = "增加商品销售数量", description = "订单完成时调用")
    public R<Void> increaseProductSaleCount(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long productId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "销售数量") @RequestParam @NotNull Integer quantity) {
        log.debug("增加商品销售数量请求，商品ID：{}，商家ID：{}，数量：{}", productId, merchantId, quantity);
        return productService.increaseSalesCount(merchantId, productId, quantity);
    }
    
    /**
     * 获取商品统计信息
     * 获取商品的浏览量、收藏数、销售数量等统计信息
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @return 统计信息
     */
    @GetMapping("/{productId}/statistics")
    @Operation(summary = "获取商品统计信息", description = "获取商品的浏览量、收藏数、销售数量等统计信息")
    public R<Map<String, Object>> getProductStatistics(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long productId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("获取商品统计信息请求，商品ID：{}，商家ID：{}", productId, merchantId);
        return productService.getProductStatistics(merchantId);
    }
    
    /**
     * 导出商品数据
     * 导出商家的商品数据到Excel
     * 
     * @param merchantId 商家ID
     * @param productName 商品名称（可选）
     * @param categoryId 分类ID（可选）
     * @param brandId 品牌ID（可选）
     * @param status 商品状态（可选）
     * @return 导出数据
     */
    @GetMapping("/export")
    @Operation(summary = "导出商品数据", description = "导出商家的商品数据到Excel")
    public R<byte[]> exportProductData(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "商品名称") @RequestParam(required = false) String productName,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "品牌") @RequestParam(required = false) String brand,
            @Parameter(description = "商品状态") @RequestParam(required = false) Integer status) {
        log.debug("导出商品数据请求，商家ID：{}，商品名称：{}", merchantId, productName);
        // 同步接口签名：品牌字段使用字符串
        return productService.exportProductData(merchantId, productName, categoryId, brand, status);
    }
    
    /**
     * 检查商品归属
     * 验证商品是否属于指定商家
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @return 检查结果
     */
    @GetMapping("/{productId}/check-ownership")
    @Operation(summary = "检查商品归属", description = "验证商品是否属于指定商家")
    public R<Boolean> checkProductOwnership(
            @Parameter(description = "商品ID") @PathVariable @NotNull Long productId,
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId) {
        log.debug("检查商品归属请求，商品ID：{}，商家ID：{}", productId, merchantId);
        return productService.checkProductOwnership(productId, merchantId);
    }
    
    /**
     * 获取库存不足商品
     * 获取库存低于指定数量的商品列表
     * 
     * @param merchantId 商家ID
     * @param threshold 库存阈值
     * @param page 页码
     * @param size 每页大小
     * @return 库存不足商品列表
     */
    @GetMapping("/low-stock")
    @Operation(summary = "获取库存不足商品", description = "获取库存低于指定数量的商品列表")
    public R<PageResult<MerchantProduct>> getLowStockProducts(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "库存阈值") @RequestParam(defaultValue = "10") Integer threshold,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        log.debug("获取库存不足商品请求，商家ID：{}，阈值：{}", merchantId, threshold);
        return productService.getLowStockProducts(merchantId, threshold, page, size);
    }
    
    /**
     * 获取热销商品
     * 获取销量排名靠前的商品列表
     * 
     * @param merchantId 商家ID
     * @param limit 数量限制
     * @return 热销商品列表
     */
    @GetMapping("/hot-selling")
    @Operation(summary = "获取热销商品", description = "获取销量排名靠前的商品列表")
    public R<List<MerchantProduct>> getHotSellingProducts(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        log.debug("获取热销商品请求，商家ID：{}，限制：{}", merchantId, limit);
        return productService.getHotSellingProducts(merchantId, limit);
    }
    
    /**
     * 获取推荐商品
     * 获取被设置为推荐的商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 推荐商品列表
     */
    @GetMapping("/recommended")
    @Operation(summary = "获取推荐商品", description = "获取被设置为推荐的商品列表")
    public R<PageResult<MerchantProduct>> getRecommendedProducts(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        log.debug("获取推荐商品请求，商家ID：{}", merchantId);
        return productService.getRecommendedProducts(merchantId, page, size);
    }
    
    /**
     * 获取新品商品
     * 获取被设置为新品的商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 新品商品列表
     */
    @GetMapping("/new-products")
    @Operation(summary = "获取新品商品", description = "获取被设置为新品的商品列表")
    public R<PageResult<MerchantProduct>> getNewProducts(
            @Parameter(description = "商家ID") @RequestParam @NotNull Long merchantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        log.debug("获取新品商品请求，商家ID：{}", merchantId);
        return productService.getNewProducts(merchantId, page, size);
    }
}