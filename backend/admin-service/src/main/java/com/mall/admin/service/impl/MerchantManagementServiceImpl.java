package com.mall.admin.service.impl;

import com.mall.admin.client.MerchantClient;
import com.mall.admin.domain.dto.MerchantApprovalRequest;
import com.mall.admin.domain.dto.MerchantQueryRequest;
import com.mall.admin.domain.entity.Merchant;
import com.mall.admin.service.MerchantManagementService;
import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 商家管理服务实现类
 * 负责管理员对商家的管理操作
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantManagementServiceImpl implements MerchantManagementService {
    
    private final MerchantClient merchantClient;
    
    /**
     * 分页查询商家列表
     * 通过Feign客户端调用商家服务获取商家数据
     * 
     * @param request 查询请求参数
     * @return 分页商家列表
     */
    @Override
    public PageResult<Merchant> getMerchantList(MerchantQueryRequest request) {
        log.info("管理员查询商家列表，参数：{}", request);
        
        try {
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("page", request.getPage());
            params.put("size", request.getSize());
            params.put("keyword", request.getKeyword());
            params.put("status", request.getStatus());
            params.put("approvalStatus", request.getApprovalStatus());
            params.put("category", request.getCategory());
            params.put("dateRange", request.getDateRange());
            params.put("startTime", request.getStartTime());
            params.put("endTime", request.getEndTime());
            
            // 调用商家服务
            R<Map<String, Object>> result = merchantClient.getMerchantList(params);
            
            if (result.isSuccess()) {
                Map<String, Object> data = result.getData();
                // 解析返回的分页数据
                return PageResult.success(
                    (java.util.List<Merchant>) data.get("list"),
                    ((Number) data.get("total")).longValue(),
                    ((Number) data.get("page")).intValue(),
                    ((Number) data.get("size")).intValue()
                );
            } else {
                log.error("调用商家服务失败：{}", result.getMsg());
                return PageResult.empty();
            }
        } catch (Exception e) {
            log.error("查询商家列表异常", e);
            return PageResult.empty();
        }
    }
    
    /**
     * 根据ID获取商家详情
     * 
     * @param merchantId 商家ID
     * @return 商家详情
     */
    @Override
    public Merchant getMerchantDetail(Long merchantId) {
        log.info("管理员查询商家详情，商家ID：{}", merchantId);
        
        try {
            R<Merchant> result = merchantClient.getMerchantDetail(merchantId);
            
            if (result.isSuccess()) {
                return result.getData();
            } else {
                log.error("获取商家详情失败：{}", result.getMsg());
                return null;
            }
        } catch (Exception e) {
            log.error("获取商家详情异常，商家ID：{}", merchantId, e);
            return null;
        }
    }
    
    /**
     * 审核商家
     * 
     * @param request 审核请求
     * @param adminId 管理员ID
     * @return 操作结果
     */
    @Override
    public boolean approveMerchant(MerchantApprovalRequest request, Long adminId) {
        log.info("管理员[{}]审核商家，请求：{}", adminId, request);
        
        try {
            // 构建审核参数
            Map<String, Object> params = new HashMap<>();
            params.put("approved", request.getApproved());
            params.put("reason", request.getReason());
            params.put("permissions", request.getPermissions());
            params.put("adminId", adminId);
            
            R<Void> result = merchantClient.approveMerchant(request.getMerchantId(), params);
            
            if (result.isSuccess()) {
                log.info("商家审核成功，商家ID：{}，审核结果：{}，操作管理员：{}", 
                    request.getMerchantId(), request.getApproved() ? "通过" : "拒绝", adminId);
                return true;
            } else {
                log.error("商家审核失败：{}", result.getMsg());
                return false;
            }
        } catch (Exception e) {
            log.error("审核商家异常，商家ID：{}，管理员ID：{}", request.getMerchantId(), adminId, e);
            return false;
        }
    }
    
    /**
     * 禁用商家
     * 
     * @param merchantId 商家ID
     * @param adminId 管理员ID
     * @return 操作结果
     */
    @Override
    public boolean disableMerchant(Long merchantId, Long adminId) {
        log.info("管理员[{}]禁用商家，商家ID：{}", adminId, merchantId);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("merchantId", merchantId);
            params.put("adminId", adminId);
            
            R<Void> result = merchantClient.disableMerchant(params);
            
            if (result.isSuccess()) {
                log.info("商家禁用成功，商家ID：{}，操作管理员：{}", merchantId, adminId);
                return true;
            } else {
                log.error("商家禁用失败：{}", result.getMsg());
                return false;
            }
        } catch (Exception e) {
            log.error("禁用商家异常，商家ID：{}，管理员ID：{}", merchantId, adminId, e);
            return false;
        }
    }
    
    /**
     * 启用商家
     * 
     * @param merchantId 商家ID
     * @param adminId 管理员ID
     * @return 操作结果
     */
    @Override
    public boolean enableMerchant(Long merchantId, Long adminId) {
        log.info("管理员[{}]启用商家，商家ID：{}", adminId, merchantId);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("merchantId", merchantId);
            params.put("adminId", adminId);
            
            R<Void> result = merchantClient.enableMerchant(params);
            
            if (result.isSuccess()) {
                log.info("商家启用成功，商家ID：{}，操作管理员：{}", merchantId, adminId);
                return true;
            } else {
                log.error("商家启用失败：{}", result.getMsg());
                return false;
            }
        } catch (Exception e) {
            log.error("启用商家异常，商家ID：{}，管理员ID：{}", merchantId, adminId, e);
            return false;
        }
    }
    
    /**
     * 删除商家
     * 
     * @param merchantId 商家ID
     * @param adminId 管理员ID
     * @return 操作结果
     */
    @Override
    public boolean deleteMerchant(Long merchantId, Long adminId) {
        log.info("管理员[{}]删除商家，商家ID：{}", adminId, merchantId);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("merchantId", merchantId);
            params.put("adminId", adminId);
            
            R<Void> result = merchantClient.deleteMerchant(params);
            
            if (result.isSuccess()) {
                log.info("商家删除成功，商家ID：{}，操作管理员：{}", merchantId, adminId);
                return true;
            } else {
                log.error("商家删除失败：{}", result.getMsg());
                return false;
            }
        } catch (Exception e) {
            log.error("删除商家异常，商家ID：{}，管理员ID：{}", merchantId, adminId, e);
            return false;
        }
    }
    
    /**
     * 导出商家数据
     * 
     * @param params 查询参数
     * @return 导出的数据
     */
    @Override
    public byte[] exportMerchants(Map<String, Object> params) {
        log.info("管理员导出商家数据，参数：{}", params);
        
        try {
            R<byte[]> result = merchantClient.exportMerchants(params);
            
            if (result.isSuccess()) {
                log.info("商家数据导出成功");
                return result.getData();
            } else {
                log.error("商家数据导出失败：{}", result.getMsg());
                return new byte[0];
            }
        } catch (Exception e) {
            log.error("导出商家数据异常", e);
            return new byte[0];
        }
    }
}