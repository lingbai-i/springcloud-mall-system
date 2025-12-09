package com.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.product.domain.entity.PriceHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 价格变更历史数据访问层接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-01
 */
@Mapper
public interface PriceHistoryMapper extends BaseMapper<PriceHistory> {
}
