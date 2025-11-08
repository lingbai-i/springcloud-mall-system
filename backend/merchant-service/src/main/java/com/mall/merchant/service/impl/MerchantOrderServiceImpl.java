package com.mall.merchant.service.impl;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.merchant.domain.entity.MerchantOrder;
import com.mall.merchant.repository.MerchantOrderRepository;
import com.mall.merchant.service.MerchantOrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 商家订单服务实现类
 * 实现商家订单相关的业务逻辑处理
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Service
@RequiredArgsConstructor
public class MerchantOrderServiceImpl implements MerchantOrderService {

  private static final Logger log = LoggerFactory.getLogger(MerchantOrderServiceImpl.class);

  private final MerchantOrderRepository orderRepository;

  /*
   * 修改日志
   * V1.1 2025-11-05：修复订单状态统计返回类型调用错误；修正订单趋势实现并补充销售趋势方法。
   * 变更原因：编译报错提示未覆盖抽象方法 getSalesTrend，且 getOrderCountByStatus 使用了错误的仓库返回类型；
   * 影响范围：订单趋势与状态统计相关接口的实现与日志输出。
   */

  /**
   * 根据ID获取订单详情
   * 
   * @param orderId    订单ID
   * @param merchantId 商家ID（用于权限验证）
   * @return 订单详情
   */
  @Override
  public R<MerchantOrder> getOrderById(Long orderId, Long merchantId) {
    log.debug("获取订单详情，订单ID：{}，商家ID：{}", orderId, merchantId);

    try {
      Optional<MerchantOrder> orderOpt = orderRepository.findById(orderId);
      if (!orderOpt.isPresent()) {
        log.warn("订单不存在，ID：{}", orderId);
        return R.fail("订单不存在");
      }

      MerchantOrder order = orderOpt.get();

      // 验证订单是否属于该商家
      if (!order.getMerchantId().equals(merchantId)) {
        log.warn("订单不属于该商家，订单ID：{}，商家ID：{}", orderId, merchantId);
        return R.fail("无权限访问该订单");
      }

      return R.ok(order);

    } catch (Exception e) {
      log.error("获取订单详情失败，订单ID：{}，错误信息：{}", orderId, e.getMessage(), e);
      return R.fail("获取订单详情失败");
    }
  }

  /**
   * 根据订单号获取订单详情
   * 
   * @param orderNo    订单号
   * @param merchantId 商家ID（用于权限验证）
   * @return 订单详情
   */
  @Override
  public R<MerchantOrder> getOrderByOrderNo(String orderNo, Long merchantId) {
    log.debug("根据订单号获取订单详情，订单号：{}，商家ID：{}", orderNo, merchantId);

    try {
      Optional<MerchantOrder> orderOpt = orderRepository.findByOrderNo(orderNo);
      if (!orderOpt.isPresent()) {
        log.warn("订单不存在，订单号：{}", orderNo);
        return R.fail("订单不存在");
      }

      MerchantOrder order = orderOpt.get();

      // 验证订单是否属于该商家
      if (!order.getMerchantId().equals(merchantId)) {
        log.warn("订单不属于该商家，订单号：{}，商家ID：{}", orderNo, merchantId);
        return R.fail("无权限访问该订单");
      }

      return R.ok(order);

    } catch (Exception e) {
      log.error("根据订单号获取订单详情失败，订单号：{}，错误信息：{}", orderNo, e.getMessage(), e);
      return R.fail("获取订单详情失败");
    }
  }

  /**
   * 分页查询订单列表
   * 
   * @param merchantId 商家ID
   * @param page       页码
   * @param size       每页大小
   * @param orderNo    订单号（可选）
   * @param status     订单状态（可选）
   * @param userId     用户ID（可选）
   * @param startTime  开始时间（可选）
   * @param endTime    结束时间（可选）
   * @return 订单分页列表
   */
  @Override
  public R<PageResult<MerchantOrder>> getOrderList(Long merchantId, Integer page, Integer size,
      String orderNo, Integer status, Long userId,
      LocalDateTime startTime, LocalDateTime endTime) {
    log.debug("分页查询订单列表，商家ID：{}，页码：{}，大小：{}", merchantId, page, size);

    try {
      Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
      // 注意：仓库方法签名为 (merchantId, orderNo, status, paymentMethod, startTime, endTime, pageable)
      // 这里不使用 userId 过滤（仓库暂不支持），保持编译通过与现有查询兼容。
      Page<MerchantOrder> orderPage = orderRepository.findByConditions(
          merchantId, orderNo, status, null, startTime, endTime, pageable);

      // 说明：统一按 common-core PageResult.of(List, Long, Long, Long) 签名传递 Long 参数，
      // 避免 Integer/Long 混用导致的编译不兼容问题；保留显式 Long.valueOf 以清晰意图。
      PageResult<MerchantOrder> result = PageResult.of(orderPage.getContent(),
          Long.valueOf(orderPage.getTotalElements()), Long.valueOf(page), Long.valueOf(size));
      return R.ok(result);

    } catch (Exception e) {
      log.error("分页查询订单列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("查询订单列表失败");
    }
  }

  /**
   * 导出订单数据为 CSV 字节数组
   * 设计说明：
   * - 使用仓库的条件查询获取数据（不分页），再序列化为 CSV 以兼容轻量导出；
   * - 避免引入重型 Excel 依赖，保持模块简洁；
   * - 字段选择聚焦业务常用维度，便于后续扩展。
   *
   * @author lingbai
   * @param merchantId 商家ID
   * @param orderNo 订单号（可选，模糊匹配）
   * @param status 订单状态（可选）
   * @param paymentMethod 支付方式（可选）
   * @param startTime 开始时间（可选）
   * @param endTime 结束时间（可选）
   * @return 包含 CSV 字节的结果包装
   */
  @Override
  public R<byte[]> exportOrderData(Long merchantId, String orderNo, Integer status, Integer paymentMethod,
                                   LocalDateTime startTime, LocalDateTime endTime) {
    log.info("导出订单数据，商家ID：{}，订单号：{}，状态：{}，支付方式：{}，时间范围：{} ~ {}",
        merchantId, orderNo, status, paymentMethod, startTime, endTime);

    try {
      // 使用不分页查询获取全部符合条件的数据
      Page<MerchantOrder> page = orderRepository.findByConditions(
          merchantId, orderNo, status, paymentMethod, startTime, endTime, Pageable.unpaged());
      List<MerchantOrder> orders = page.getContent();

      // 构建 CSV 内容（含表头）
      StringBuilder sb = new StringBuilder();
      sb.append("orderNo,status,totalAmount,paidAmount,paymentMethod,shipTime,finishTime,logisticsCompany,logisticsNo\n");
      for (MerchantOrder o : orders) {
        // 简单的 CSV 序列化，空值处理为空字符串，时间使用 toString()
        sb.append(safeCsv(o.getOrderNo())).append(',')
          .append(String.valueOf(o.getStatus())).append(',')
          .append(o.getTotalAmount() != null ? o.getTotalAmount() : java.math.BigDecimal.ZERO).append(',')
          .append(o.getPaidAmount() != null ? o.getPaidAmount() : java.math.BigDecimal.ZERO).append(',')
          .append(o.getPaymentMethod() != null ? String.valueOf(o.getPaymentMethod()) : "").append(',')
          .append(o.getShipTime() != null ? o.getShipTime().toString() : "").append(',')
          .append(o.getFinishTime() != null ? o.getFinishTime().toString() : "").append(',')
          .append(safeCsv(o.getLogisticsCompany())).append(',')
          .append(safeCsv(o.getLogisticsNo()))
          .append("\n");
      }

      byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
      log.info("导出订单数据完成，商家ID：{}，导出条数：{}，字节大小：{}", merchantId, orders.size(), bytes.length);
      return R.ok(bytes);

    } catch (Exception e) {
      log.error("导出订单数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("导出订单数据失败");
    }
  }

  // CSV 安全写入：处理逗号与换行，保持简单可读
  private String safeCsv(String v) {
    if (v == null) {
      return "";
    }
    String s = v.replace('\n', ' ').replace('\r', ' ');
    if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0) {
      return '"' + s.replace("\"", "\"\"") + '"';
    }
    return s;
  }

  /**
   * 获取待发货订单列表
   * 
   * @param merchantId 商家ID
   * @param page       页码
   * @param size       每页大小
   * @return 待发货订单列表
   */
  @Override
  public R<PageResult<MerchantOrder>> getPendingShipmentOrders(Long merchantId, Integer page, Integer size) {
    log.debug("获取待发货订单列表，商家ID：{}，页码：{}，大小：{}", merchantId, page, size);

    try {
      Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "payTime"));
      Page<MerchantOrder> orderPage = orderRepository.findPendingShipmentOrders(merchantId, pageable);

      // 说明：统一按 common-core PageResult.of(List, Long, Long, Long) 签名传递 Long 参数
      PageResult<MerchantOrder> result = PageResult.of(orderPage.getContent(),
          Long.valueOf(orderPage.getTotalElements()), Long.valueOf(page), Long.valueOf(size));
      return R.ok(result);

    } catch (Exception e) {
      log.error("获取待发货订单列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("获取待发货订单列表失败");
    }
  }

  /**
   * 获取待收货订单列表
   * 
   * @param merchantId 商家ID
   * @param page       页码
   * @param size       每页大小
   * @return 待收货订单列表
   */
  @Override
  public R<PageResult<MerchantOrder>> getPendingReceiptOrders(Long merchantId, Integer page, Integer size) {
    log.debug("获取待收货订单列表，商家ID：{}，页码：{}，大小：{}", merchantId, page, size);

    try {
      Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "shipTime"));
      Page<MerchantOrder> orderPage = orderRepository.findPendingReceiptOrders(merchantId, pageable);

      // 说明：统一按 common-core PageResult.of(List, Long, Long, Long) 签名传递 Long 参数
      PageResult<MerchantOrder> result = PageResult.of(orderPage.getContent(),
          Long.valueOf(orderPage.getTotalElements()), Long.valueOf(page), Long.valueOf(size));
      return R.ok(result);

    } catch (Exception e) {
      log.error("获取待收货订单列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("获取待收货订单列表失败");
    }
  }

  /**
   * 获取退款订单列表
   * 
   * @param merchantId 商家ID
   * @param page       页码
   * @param size       每页大小
   * @return 退款订单列表
   */
  @Override
  public R<PageResult<MerchantOrder>> getRefundOrders(Long merchantId, Integer page, Integer size) {
    log.debug("获取退款订单列表，商家ID：{}，页码：{}，大小：{}", merchantId, page, size);

    try {
      Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "refundTime"));
      Page<MerchantOrder> orderPage = orderRepository.findRefundOrders(merchantId, pageable);

      // 说明：统一按 common-core PageResult.of(List, Long, Long, Long) 签名传递 Long 参数
      PageResult<MerchantOrder> result = PageResult.of(orderPage.getContent(),
          Long.valueOf(orderPage.getTotalElements()), Long.valueOf(page), Long.valueOf(size));
      return R.ok(result);

    } catch (Exception e) {
      log.error("获取退款订单列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("获取退款订单列表失败");
    }
  }

  /**
   * 获取最近订单列表
   * 
   * @param merchantId 商家ID
   * @param limit      限制数量
   * @return 最近订单列表
   */
  @Override
  public R<List<MerchantOrder>> getRecentOrders(Long merchantId, Integer limit) {
    log.debug("获取最近订单列表，商家ID：{}，限制：{}", merchantId, limit);

    try {
      Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createTime"));
      Page<MerchantOrder> orderPage = orderRepository.findRecentOrders(merchantId, pageable);
      return R.ok(orderPage.getContent());

    } catch (Exception e) {
      log.error("获取最近订单列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("获取最近订单列表失败");
    }
  }

  /**
   * 发货
   * 
   * @param merchantId       商家ID
   * @param orderId          订单ID
   * @param logisticsCompany 物流公司
   * @param logisticsNo      物流单号
   * @param merchantRemark   商家备注
   * @return 发货结果
   */
  @Override
  @Transactional
  public R<Void> shipOrder(Long merchantId, Long orderId, String logisticsCompany, String logisticsNo,
      String merchantRemark) {
    log.info("订单发货，商家ID：{}，订单ID：{}，物流公司：{}，物流单号：{}，备注：{}",
        merchantId, orderId, logisticsCompany, logisticsNo, merchantRemark);

    try {
      Optional<MerchantOrder> orderOpt = orderRepository.findById(orderId);
      if (!orderOpt.isPresent()) {
        log.warn("订单不存在，ID：{}", orderId);
        return R.fail("订单不存在");
      }

      MerchantOrder order = orderOpt.get();

      // 验证订单是否属于该商家
      if (!order.getMerchantId().equals(merchantId)) {
        log.warn("订单不属于该商家，订单ID：{}，商家ID：{}", orderId, merchantId);
        return R.fail("无权限操作该订单");
      }

      // 检查订单状态是否可以发货
      if (!order.canShip()) {
        log.warn("订单状态不允许发货，订单ID：{}，状态：{}", orderId, order.getStatus());
        return R.fail("订单状态不允许发货");
      }

      // 更新订单状态和物流信息
      order.setStatus(3); // 已发货
      order.setShipTime(LocalDateTime.now());
      order.setLogisticsCompany(logisticsCompany);
      order.setLogisticsNo(logisticsNo);
      if (merchantRemark != null && !merchantRemark.trim().isEmpty()) {
        order.setMerchantRemark(merchantRemark);
      }

      orderRepository.save(order);

      log.info("订单发货成功，订单ID：{}", orderId);
      return R.ok();

    } catch (Exception e) {
      log.error("订单发货失败，订单ID：{}，错误信息：{}", orderId, e.getMessage(), e);
      return R.fail("发货失败，请稍后重试");
    }
  }

  /**
   * 批量发货
   * 
   * @param merchantId    商家ID
   * @param shipmentInfos 发货信息列表，包含订单ID、物流公司、物流单号等
   * @return 批量发货结果
   */
  @Override
  @Transactional
  public R<Void> batchShipOrders(Long merchantId, List<Map<String, Object>> shipmentInfos) {
    log.info("批量发货，商家ID：{}，订单数量：{}", merchantId, shipmentInfos.size());

    try {
      int successCount = 0;
      int failCount = 0;

      for (Map<String, Object> shipmentInfo : shipmentInfos) {
        try {
          Long orderId = Long.valueOf(shipmentInfo.get("orderId").toString());
          String logisticsCompany = (String) shipmentInfo.get("logisticsCompany");
          String trackingNumber = (String) shipmentInfo.get("trackingNumber");
          String merchantRemark = (String) shipmentInfo.get("merchantRemark");

          R<Void> result = shipOrder(merchantId, orderId, logisticsCompany, trackingNumber, merchantRemark);
          if (result.isSuccess()) {
            successCount++;
          } else {
            failCount++;
            log.warn("订单发货失败，订单ID：{}，原因：{}", orderId, result.getMessage());
          }
        } catch (Exception e) {
          failCount++;
          log.error("订单发货异常，错误信息：{}", e.getMessage(), e);
        }
      }

      log.info("批量发货完成，商家ID：{}，成功：{}，失败：{}", merchantId, successCount, failCount);
      if (failCount > 0) {
        return R.fail(String.format("批量发货完成，成功：%d，失败：%d", successCount, failCount));
      }
      return R.ok();

    } catch (Exception e) {
      log.error("批量发货失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("批量发货失败，请稍后重试");
    }
  }

  /**
   * 确认收货
   * 
   * @param orderId    订单ID
   * @param merchantId 商家ID
   * @return 确认收货结果
   */
  @Override
  @Transactional
  public R<Void> confirmReceipt(Long merchantId, Long orderId) {
    log.info("确认收货，商家ID：{}，订单ID：{}", merchantId, orderId);

    try {
      Optional<MerchantOrder> orderOpt = orderRepository.findById(orderId);
      if (!orderOpt.isPresent()) {
        log.warn("订单不存在，ID：{}", orderId);
        return R.fail("订单不存在");
      }

      MerchantOrder order = orderOpt.get();

      // 验证订单是否属于该商家
      if (!order.getMerchantId().equals(merchantId)) {
        log.warn("订单不属于该商家，订单ID：{}，商家ID：{}", orderId, merchantId);
        return R.fail("无权限操作该订单");
      }

      // 检查订单状态
      if (order.getStatus() != 3) {
        log.warn("订单状态不允许确认收货，订单ID：{}，状态：{}", orderId, order.getStatus());
        return R.fail("订单状态不允许确认收货");
      }

      // 更新订单状态
      order.setStatus(4); // 已收货
      order.setReceiveTime(LocalDateTime.now());

      orderRepository.save(order);

      log.info("确认收货成功，订单ID：{}", orderId);
      return R.ok();

    } catch (Exception e) {
      log.error("确认收货失败，订单ID：{}，错误信息：{}", orderId, e.getMessage(), e);
      return R.fail("确认收货失败，请稍后重试");
    }
  }

  /**
   * 取消订单
   * 
   * @param orderId    订单ID
   * @param merchantId 商家ID
   * @param reason     取消原因
   * @return 取消结果
   */
  @Override
  @Transactional
  public R<Void> cancelOrder(Long orderId, Long merchantId, String reason) {
    log.info("取消订单，订单ID：{}，商家ID：{}，原因：{}", orderId, merchantId, reason);

    try {
      Optional<MerchantOrder> orderOpt = orderRepository.findById(orderId);
      if (!orderOpt.isPresent()) {
        log.warn("订单不存在，ID：{}", orderId);
        return R.fail("订单不存在");
      }

      MerchantOrder order = orderOpt.get();

      // 验证订单是否属于该商家
      if (!order.getMerchantId().equals(merchantId)) {
        log.warn("订单不属于该商家，订单ID：{}，商家ID：{}", orderId, merchantId);
        return R.fail("无权限操作该订单");
      }

      // 检查订单状态是否可以取消
      if (!order.canCancel()) {
        log.warn("订单状态不允许取消，订单ID：{}，状态：{}", orderId, order.getStatus());
        return R.fail("订单状态不允许取消");
      }

      // 更新订单状态
      order.setStatus(6); // 已取消
      order.setCancelTime(LocalDateTime.now());
      order.setRefundReason(reason);

      orderRepository.save(order);

      log.info("取消订单成功，订单ID：{}", orderId);
      return R.ok();

    } catch (Exception e) {
      log.error("取消订单失败，订单ID：{}，错误信息：{}", orderId, e.getMessage(), e);
      return R.fail("取消订单失败，请稍后重试");
    }
  }

  /**
   * 处理退款申请
   * 
   * @param merchantId   商家ID
   * @param orderId      订单ID
   * @param agree        是否同意退款
   * @param refundReason 退款原因
   * @return 处理结果
   */
  @Override
  @Transactional
  public R<Void> handleRefundRequest(Long merchantId, Long orderId, Boolean agree, String refundReason) {
    log.info("处理退款申请，订单ID：{}，商家ID：{}，是否同意：{}，原因：{}",
        orderId, merchantId, agree, refundReason);

    try {
      Optional<MerchantOrder> orderOpt = orderRepository.findById(orderId);
      if (!orderOpt.isPresent()) {
        log.warn("订单不存在，ID：{}", orderId);
        return R.fail("订单不存在");
      }

      MerchantOrder order = orderOpt.get();

      // 验证订单是否属于该商家
      if (!order.getMerchantId().equals(merchantId)) {
        log.warn("订单不属于该商家，订单ID：{}，商家ID：{}", orderId, merchantId);
        return R.fail("无权限操作该订单");
      }

      // 检查订单状态是否可以退款
      if (!order.canRefund()) {
        log.warn("订单状态不允许退款，订单ID：{}，状态：{}", orderId, order.getStatus());
        return R.fail("订单状态不允许退款");
      }

      // 更新订单状态
      if (agree) {
        order.setRefundStatus(2); // 退款成功
        order.setRefundAmount(order.getPaidAmount()); // 全额退款
        order.setRefundTime(LocalDateTime.now());
      } else {
        order.setRefundStatus(3); // 退款失败
      }
      order.setRefundReason(refundReason);

      orderRepository.save(order);

      log.info("处理退款成功，订单ID：{}", orderId);
      return R.ok();

    } catch (Exception e) {
      log.error("处理退款失败，订单ID：{}，错误信息：{}", orderId, e.getMessage(), e);
      return R.fail("处理退款失败，请稍后重试");
    }
  }

  /**
   * 更新订单备注
   * 
   * @param orderId    订单ID
   * @param merchantId 商家ID
   * @param remark     备注
   * @return 更新结果
   */
  @Override
  @Transactional
  public R<Void> updateOrderRemark(Long merchantId, Long orderId, String merchantRemark) {
    log.info("更新订单备注，商家ID：{}，订单ID：{}", merchantId, orderId);

    try {
      Optional<MerchantOrder> orderOpt = orderRepository.findById(orderId);
      if (!orderOpt.isPresent()) {
        log.warn("订单不存在，ID：{}", orderId);
        return R.fail("订单不存在");
      }

      MerchantOrder order = orderOpt.get();

      // 验证订单是否属于该商家
      if (!order.getMerchantId().equals(merchantId)) {
        log.warn("订单不属于该商家，订单ID：{}，商家ID：{}", orderId, merchantId);
        return R.fail("无权限操作该订单");
      }

      order.setMerchantRemark(merchantRemark);
      orderRepository.save(order);

      log.info("更新订单备注成功，订单ID：{}", orderId);
      return R.ok();

    } catch (Exception e) {
      log.error("更新订单备注失败，订单ID：{}，错误信息：{}", orderId, e.getMessage(), e);
      return R.fail("更新订单备注失败，请稍后重试");
    }
  }

  /**
   * 更新物流信息
   * 
   * @param merchantId    商家ID
   * @param orderId       订单ID
   * @param logisticsInfo 物流信息
   * @return 更新结果
   */
  @Override
  @Transactional
  public R<Void> updateLogisticsInfo(Long merchantId, Long orderId, Map<String, Object> logisticsInfo) {
    log.info("更新物流信息，商家ID：{}，订单ID：{}，物流信息：{}",
        merchantId, orderId, logisticsInfo);

    try {
      Optional<MerchantOrder> orderOpt = orderRepository.findById(orderId);
      if (!orderOpt.isPresent()) {
        log.warn("订单不存在，ID：{}", orderId);
        return R.fail("订单不存在");
      }

      MerchantOrder order = orderOpt.get();

      // 验证订单是否属于该商家
      if (!order.getMerchantId().equals(merchantId)) {
        log.warn("订单不属于该商家，订单ID：{}，商家ID：{}", orderId, merchantId);
        return R.fail("无权限操作该订单");
      }

      // 从Map中提取物流信息
      String logisticsCompany = (String) logisticsInfo.get("logisticsCompany");
      String trackingNumber = (String) logisticsInfo.get("trackingNumber");

      if (logisticsCompany != null) {
        order.setLogisticsCompany(logisticsCompany);
      }
      if (trackingNumber != null) {
        order.setLogisticsNo(trackingNumber);
      }

      orderRepository.save(order);

      log.info("更新物流信息成功，订单ID：{}", orderId);
      return R.ok();

    } catch (Exception e) {
      log.error("更新物流信息失败，订单ID：{}，错误信息：{}", orderId, e.getMessage(), e);
      return R.fail("更新物流信息失败，请稍后重试");
    }
  }

  /**
   * 获取订单统计数据
   * 
   * @param merchantId 商家ID
   * @param startTime  开始时间（可选）
   * @param endTime    结束时间（可选）
   * @return 统计数据
   */
  @Override
  public R<Map<String, Object>> getOrderStatistics(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
    log.debug("获取订单统计数据，商家ID：{}，开始时间：{}，结束时间：{}", merchantId, startTime, endTime);

    try {
      Map<String, Object> statistics = new HashMap<>();

      // 总订单数
      Long totalOrders = orderRepository.countByMerchantId(merchantId);
      statistics.put("totalOrders", totalOrders);

      // 待付款订单数
      Long pendingPayment = orderRepository.countByMerchantIdAndStatus(merchantId, 1);
      statistics.put("pendingPayment", pendingPayment);

      // 待发货订单数
      Long pendingShipment = orderRepository.countByMerchantIdAndStatus(merchantId, 2);
      statistics.put("pendingShipment", pendingShipment);

      // 待收货订单数
      Long pendingReceipt = orderRepository.countByMerchantIdAndStatus(merchantId, 3);
      statistics.put("pendingReceipt", pendingReceipt);

      // 已完成订单数
      Long completed = orderRepository.countByMerchantIdAndStatus(merchantId, 5);
      statistics.put("completed", completed);

      // 已取消订单数
      Long cancelled = orderRepository.countByMerchantIdAndStatus(merchantId, 6);
      statistics.put("cancelled", cancelled);

      // 今日订单数
      LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
      LocalDateTime todayEnd = todayStart.plusDays(1);
      // 今日订单数
      Long todayOrders = orderRepository.countOrdersByTimeRange(merchantId, todayStart, todayEnd);
      statistics.put("todayOrders", todayOrders);

      // 今日销售额
      BigDecimal todaySales = orderRepository.sumSalesByDateRange(merchantId, todayStart, todayEnd);
      statistics.put("todaySales", todaySales != null ? todaySales : BigDecimal.ZERO);

      // 本月订单数
      LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
          .withNano(0);
      LocalDateTime monthEnd = monthStart.plusMonths(1);
      Long monthOrders = orderRepository.countOrdersByTimeRange(merchantId, monthStart, monthEnd);
      statistics.put("monthOrders", monthOrders);

      // 本月销售额
      BigDecimal monthSales = orderRepository.sumSalesByDateRange(merchantId, monthStart, monthEnd);
      statistics.put("monthSales", monthSales != null ? monthSales : BigDecimal.ZERO);

      return R.ok(statistics);

    } catch (Exception e) {
      log.error("获取订单统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("获取统计数据失败");
    }
  }

  /**
   * 获取订单状态统计
   * 
   * @param merchantId 商家ID
   * @return 状态统计数据
   */
  @Override
  public R<Map<Integer, Long>> getOrderCountByStatus(Long merchantId) {
    log.debug("获取订单状态统计，商家ID：{}", merchantId);

    try {
      // 调整为直接使用仓库层提供的 Map 格式结果，避免类型不匹配
      // 选择该方式的原因：仓库已提供封装方法，减少重复转换逻辑与潜在类型错误
      Map<Integer, Long> statusCount = orderRepository.findOrderCountByStatus(merchantId);
      return R.ok(statusCount);

    } catch (Exception e) {
      log.error("获取订单状态统计失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("获取订单状态统计失败");
    }
  }

  @Override
  public R<Boolean> checkOrderOwnership(Long orderId, Long merchantId) {
    log.debug("检查订单归属，订单ID：{}，商家ID：{}", orderId, merchantId);

    try {
      boolean belongs = orderRepository.existsByIdAndMerchantId(orderId, merchantId);
      return R.ok(belongs);

    } catch (Exception e) {
      log.error("检查订单归属失败，订单ID：{}，商家ID：{}，错误信息：{}", orderId, merchantId, e.getMessage(), e);
      return R.fail("检查失败");
    }
  }

  /**
   * 获取订单物流信息
   * 基于订单归属校验后返回物流公司、物流单号、发货时间与订单状态等信息。
   * 设计说明：优先进行归属校验以保证数据安全，其次再查询订单详情；
   * 返回结构采用 Map 以便前端灵活映射与展示。
   * 文档生成时间：2025-11-05T19:52:02+08:00
   *
   * @author lingbai
   * @param merchantId 商家ID（不可为空）
   * @param orderId    订单ID（不可为空）
   * @return 物流信息（orderId、orderNo、logisticsCompany、logisticsNo、shipTime、status）
   */
  @Override
  public R<Map<String, Object>> getLogisticsInfo(Long merchantId, Long orderId) {
    log.info("获取订单物流信息，商家ID：{}，订单ID：{}", merchantId, orderId);

    try {
      // 业务安全：先校验订单归属，防止越权访问
      R<Boolean> ownershipCheck = checkOrderOwnership(orderId, merchantId);
      if (ownershipCheck.getData() == null || !ownershipCheck.getData()) {
        log.warn("订单归属验证失败，商家ID：{}，订单ID：{}", merchantId, orderId);
        return R.fail("无权限访问该订单");
      }

      // 查询订单详情（使用仓库层，避免额外依赖）
      Optional<MerchantOrder> orderOpt = orderRepository.findById(orderId);
      if (orderOpt.isEmpty()) {
        log.warn("订单不存在，订单ID：{}", orderId);
        return R.fail("订单不存在");
      }

      MerchantOrder order = orderOpt.get();

      // 构建物流信息返回结构
      Map<String, Object> logisticsInfo = new HashMap<>();
      logisticsInfo.put("orderId", orderId);
      logisticsInfo.put("orderNo", order.getOrderNo());
      logisticsInfo.put("logisticsCompany", order.getLogisticsCompany());
      logisticsInfo.put("logisticsNo", order.getLogisticsNo());
      logisticsInfo.put("shipTime", order.getShipTime());
      logisticsInfo.put("status", order.getStatus());

      log.info("获取订单物流信息成功，商家ID：{}，订单ID：{}", merchantId, orderId);
      return R.ok(logisticsInfo);
    } catch (Exception e) {
      // 错误处理：记录完整堆栈，避免吞错，同时返回业务友好的信息
      log.error("获取订单物流信息失败，商家ID：{}，订单ID：{}，错误信息：{}", merchantId, orderId, e.getMessage(), e);
      return R.fail("获取物流信息失败");
    }
  }

  /**
   * 获取每月销售统计
   * 
   * @param merchantId 商家ID
   * @param startMonth 开始月份
   * @param endMonth 结束月份
   * @return 每月销售统计
   */
  @Override
  public R<List<Map<String, Object>>> getMonthlySalesStatistics(Long merchantId, String startMonth, String endMonth) {
    log.debug("获取每月销售统计，商家ID：{}，开始月份：{}，结束月份：{}", merchantId, startMonth, endMonth);
    
    try {
      // 简单实现：返回空列表，后续可根据实际需求完善
      return R.ok(new ArrayList<>());
    } catch (Exception e) {
      log.error("获取每月销售统计失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("获取每月销售统计失败");
    }
  }

  /**
   * 获取每日销售统计
   * 
   * @param merchantId 商家ID
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @return 每日销售统计
   */
  @Override
  public R<List<Map<String, Object>>> getDailySalesStatistics(Long merchantId, LocalDate startDate, LocalDate endDate) {
    log.debug("获取每日销售统计，商家ID：{}，开始日期：{}，结束日期：{}", merchantId, startDate, endDate);
    
    try {
      // 简单实现：返回空列表，后续可根据实际需求完善
      return R.ok(new ArrayList<>());
    } catch (Exception e) {
      log.error("获取每日销售统计失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("获取每日销售统计失败");
    }
  }

  /**
   * 获取客户购买统计
   * 
   * @param merchantId 商家ID
   * @param topCount 返回前N个客户
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 客户购买统计
   */
  @Override
  public R<List<Map<String, Object>>> getCustomerPurchaseStatistics(Long merchantId, Integer topCount, LocalDateTime startTime, LocalDateTime endTime) {
    log.debug("获取客户购买统计，商家ID：{}，前N个客户：{}，时间范围：{} ~ {}", merchantId, topCount, startTime, endTime);
    
    try {
      // 简单实现：返回空列表，后续可根据实际需求完善
      return R.ok(new ArrayList<>());
    } catch (Exception e) {
      log.error("获取客户购买统计失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("获取客户购买统计失败");
    }
  }

  /**
   * 获取热销商品统计
   * 
   * @param merchantId 商家ID
   * @param topCount 返回前N个商品
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 热销商品统计
   */
  @Override
  public R<List<Map<String, Object>>> getHotProductsStatistics(Long merchantId, Integer topCount, LocalDateTime startTime, LocalDateTime endTime) {
    log.debug("获取热销商品统计，商家ID：{}，前N个商品：{}，时间范围：{} ~ {}", merchantId, topCount, startTime, endTime);
    
    try {
      // 简单实现：返回空列表，后续可根据实际需求完善
      return R.ok(new ArrayList<>());
    } catch (Exception e) {
      log.error("获取热销商品统计失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("获取热销商品统计失败");
    }
  }

  /**
   * 获取订单状态统计
   * 
   * @param merchantId 商家ID
   * @return 订单状态统计
   */
  @Override
  public R<Map<String, Object>> getOrderStatusStatistics(Long merchantId) {
    log.debug("获取订单状态统计，商家ID：{}", merchantId);
    
    try {
      // 简单实现：返回空Map，后续可根据实际需求完善
      return R.ok(new HashMap<>());
    } catch (Exception e) {
      log.error("获取订单状态统计失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("获取订单状态统计失败");
    }
  }

  @Override
  public R<List<Map<String, Object>>> getOrderTrend(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
    log.debug("获取订单趋势数据，商家ID：{}，开始时间：{}，结束时间：{}", merchantId, startTime, endTime);

    try {
      // 使用每日销售统计作为订单趋势基础数据（包含订单数与总金额）
      // 选择该查询的原因：现有仓库方法提供了日期、订单数、销售额三项数据，满足趋势展示需求
      List<Object[]> rawData = orderRepository.findDailySalesStatistics(merchantId, startTime, endTime);
      List<Map<String, Object>> orderTrend = rawData.stream()
          .map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0]);
            item.put("orderCount", row[1]);
            item.put("totalAmount", row[2]);
            return item;
          })
          .collect(java.util.stream.Collectors.toList());
      return R.ok(orderTrend);

    } catch (Exception e) {
      // 明确为“订单趋势”错误，便于区分与销售趋势日志
      log.error("获取订单趋势数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("获取订单趋势数据失败");
    }
  }

  /**
   * 获取销售趋势
   * 提供指定时间范围内按日聚合的销售趋势数据（日期、订单数、总金额）。
   * 设计说明：复用每日销售统计查询，保证与订单趋势一致的时间粒度输出；
   * 同时返回订单数与总金额，方便前端灵活展示曲线或柱状图。
   *
   * @author lingbai
   * @param merchantId 商家ID（不能为空）
   * @param startTime  开始时间（不能为空）
   * @param endTime    结束时间（不能为空）
   * @return 销售趋势数据列表，每项包含 date、orderCount、totalAmount
   */
  @Override
  public R<List<Map<String, Object>>> getSalesTrend(Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
    log.debug("获取销售趋势数据，商家ID：{}，开始时间：{}，结束时间：{}", merchantId, startTime, endTime);

    try {
      // 与订单趋势相同的基础查询，强调销售额维度
      List<Object[]> rawData = orderRepository.findDailySalesStatistics(merchantId, startTime, endTime);
      List<Map<String, Object>> salesTrend = rawData.stream()
          .map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0]);
            item.put("orderCount", row[1]); // 保留订单数，支持多图联动
            item.put("totalAmount", row[2]);
            return item;
          })
          .collect(java.util.stream.Collectors.toList());
      return R.ok(salesTrend);

    } catch (Exception e) {
      log.error("获取销售趋势数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
      return R.fail("获取销售趋势数据失败");
    }
  }
}
