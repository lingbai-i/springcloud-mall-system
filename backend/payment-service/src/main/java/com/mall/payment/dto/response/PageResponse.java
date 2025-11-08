package com.mall.payment.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 分页响应DTO
 * 用于返回分页查询结果的通用响应对象
 * 
 * <p>分页信息说明：</p>
 * <ul>
 *   <li>records：当前页的数据列表</li>
 *   <li>total：总记录数</li>
 *   <li>page：当前页码（从1开始）</li>
 *   <li>size：每页大小</li>
 *   <li>pages：总页数</li>
 *   <li>hasPrevious：是否有上一页</li>
 *   <li>hasNext：是否有下一页</li>
 *   <li>isFirst：是否为第一页</li>
 *   <li>isLast：是否为最后一页</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>支付订单分页查询</li>
 *   <li>退款订单分页查询</li>
 *   <li>支付记录分页查询</li>
 *   <li>其他需要分页的查询接口</li>
 * </ul>
 * 
 * <p>计算规则：</p>
 * <ul>
 *   <li>总页数 = (总记录数 + 每页大小 - 1) / 每页大小</li>
 *   <li>是否有上一页 = 当前页 > 1</li>
 *   <li>是否有下一页 = 当前页 < 总页数</li>
 *   <li>是否为第一页 = 当前页 == 1</li>
 *   <li>是否为最后一页 = 当前页 == 总页数</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * @param <T> 数据类型泛型，支持任意类型的分页数据
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加分页信息说明和计算规则
 * V1.1 2024-12-08：增加分页状态判断字段
 * V1.0 2024-12-01：初始版本，定义基本分页响应结构
 */
@Data
public class PageResponse<T> {

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否为第一页
     */
    private Boolean isFirst;

    /**
     * 是否为最后一页
     */
    private Boolean isLast;

    /**
     * 默认构造函数
     */
    public PageResponse() {
    }

    /**
     * 构造函数
     * 
     * @param records 数据列表
     * @param total 总记录数
     * @param page 当前页码
     * @param size 每页大小
     */
    public PageResponse(List<T> records, Long total, Integer page, Integer size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.calculatePages();
    }

    /**
     * 计算分页相关信息
     */
    private void calculatePages() {
        if (total == null || size == null || size <= 0) {
            this.pages = 0;
            this.hasPrevious = false;
            this.hasNext = false;
            this.isFirst = true;
            this.isLast = true;
            return;
        }

        // 计算总页数
        this.pages = (int) Math.ceil((double) total / size);
        
        // 确保页码有效
        if (page == null || page < 1) {
            this.page = 1;
        }
        if (this.page > this.pages && this.pages > 0) {
            this.page = this.pages;
        }

        // 计算分页状态
        this.hasPrevious = this.page > 1;
        this.hasNext = this.page < this.pages;
        this.isFirst = this.page == 1;
        this.isLast = this.page.equals(this.pages) || this.pages == 0;
    }

    /**
     * 创建空的分页响应
     * 
     * @param <T> 数据类型
     * @return 空的分页响应
     */
    public static <T> PageResponse<T> empty() {
        return new PageResponse<>(List.of(), 0L, 1, 10);
    }

    /**
     * 创建分页响应
     * 
     * @param records 数据列表
     * @param total 总记录数
     * @param page 当前页码
     * @param size 每页大小
     * @param <T> 数据类型
     * @return 分页响应
     */
    public static <T> PageResponse<T> of(List<T> records, Long total, Integer page, Integer size) {
        return new PageResponse<>(records, total, page, size);
    }

    /**
     * 获取当前页的记录数
     * 
     * @return 当前页的记录数
     */
    public int getCurrentPageSize() {
        return records != null ? records.size() : 0;
    }

    /**
     * 获取偏移量
     * 
     * @return 偏移量
     */
    public long getOffset() {
        if (page == null || size == null) {
            return 0;
        }
        return (long) (page - 1) * size;
    }

    /**
     * 判断是否有数据
     * 
     * @return 如果有数据返回true，否则返回false
     */
    public boolean hasContent() {
        return records != null && !records.isEmpty();
    }

    /**
     * 判断是否为空
     * 
     * @return 如果为空返回true，否则返回false
     */
    public boolean isEmpty() {
        return !hasContent();
    }

    /**
     * 获取分页信息摘要
     * 
     * @return 分页信息摘要字符串
     */
    public String getPageSummary() {
        if (total == null || total == 0) {
            return "暂无数据";
        }
        
        long startIndex = getOffset() + 1;
        long endIndex = Math.min(getOffset() + getCurrentPageSize(), total);
        
        return String.format("第 %d-%d 条，共 %d 条记录，第 %d/%d 页", 
                startIndex, endIndex, total, page, pages);
    }

    /**
     * 获取下一页页码
     * 
     * @return 下一页页码，如果没有下一页返回null
     */
    public Integer getNextPage() {
        return hasNext ? page + 1 : null;
    }

    /**
     * 获取上一页页码
     * 
     * @return 上一页页码，如果没有上一页返回null
     */
    public Integer getPreviousPage() {
        return hasPrevious ? page - 1 : null;
    }

    /**
     * 设置数据并重新计算分页信息
     * 
     * @param records 数据列表
     * @param total 总记录数
     * @param page 当前页码
     * @param size 每页大小
     */
    public void setData(List<T> records, Long total, Integer page, Integer size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.calculatePages();
    }
}