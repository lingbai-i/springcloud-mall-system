package com.mall.admin.client;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商家服务Feign Client
 * 
 * @author lingbai
 * @since 2025-01-09
 */
@FeignClient(name = "merchant-service", path = "/api/admin/merchants")
public interface MerchantServiceClient {

        /**
         * 查询商家列表
         * 
         * @param page    页码
         * @param size    每页大小
         * @param keyword 关键词
         * @param status  状态
         * @return 商家列表
         */
        @GetMapping("")
        R<PageResult<Map<String, Object>>> getMerchantList(
                        @RequestParam(value = "page", defaultValue = "1") Integer page,
                        @RequestParam(value = "size", defaultValue = "10") Integer size,
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "status", required = false) Integer status);

        /**
         * 获取商家详情
         * 
         * @param merchantId 商家ID
         * @return 商家详情
         */
        @GetMapping("/{id}")
        R<Map<String, Object>> getMerchantDetail(@PathVariable("id") Long merchantId);

        /**
         * 审核商家
         * 
         * @param merchantId 商家ID
         * @param approved   是否通过
         * @param reason     审核备注
         * @return 操作结果
         */
        @PutMapping("/{id}/audit")
        R<Void> auditMerchant(
                        @PathVariable("id") Long merchantId,
                        @RequestParam("approved") Boolean approved,
                        @RequestParam(value = "reason", required = false) String reason);

        /**
         * 获取商家统计数据
         * 
         * @return 统计数据
         */
        @GetMapping("/statistics")
        R<Map<String, Object>> getMerchantStatistics();

        /**
         * 获取商家申请列表
         * 
         * @param page    页码
         * @param size    每页大小
         * @param status  审批状态
         * @param keyword 关键词
         * @return 申请列表
         */
        @GetMapping("/applications")
        R<Map<String, Object>> getApplicationList(
                        @RequestParam(value = "page", defaultValue = "1") Integer page,
                        @RequestParam(value = "size", defaultValue = "20") Integer size,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "keyword", required = false) String keyword);

        /**
         * 获取申请详情
         * 
         * @param id 申请ID
         * @return 申请详情
         */
        @GetMapping("/applications/{id}")
        R<Map<String, Object>> getApplicationDetail(@PathVariable("id") Long id);

        /**
         * 审核申请
         * 
         * @param id        申请ID
         * @param approved  是否通过
         * @param reason    审核原因
         * @param adminId   管理员ID
         * @param adminName 管理员名称
         * @return 操作结果
         */
        @PutMapping("/applications/{id}/audit")
        R<Void> auditApplication(
                        @PathVariable("id") Long id,
                        @RequestParam("approved") Boolean approved,
                        @RequestParam(value = "reason", required = false) String reason,
                        @RequestParam("adminId") Long adminId,
                        @RequestParam("adminName") String adminName);

        /**
         * 获取申请统计
         * 
         * @return 统计数据
         */
        @GetMapping("/applications/stats")
        R<Map<String, Object>> getApplicationStats();
}
