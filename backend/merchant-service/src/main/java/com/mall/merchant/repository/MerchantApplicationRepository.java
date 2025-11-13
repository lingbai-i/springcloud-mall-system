package com.mall.merchant.repository;

import com.mall.merchant.domain.entity.MerchantApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商家申请Repository
 * 
 * @author system
 * @since 2025-11-12
 */
@Repository
public interface MerchantApplicationRepository extends JpaRepository<MerchantApplication, Long> {

       /**
        * 根据用户名查找申请
        */
       Optional<MerchantApplication> findByUsername(String username);

       /**
        * 根据用户名查找所有申请（包括历史记录）
        */
       List<MerchantApplication> findAllByUsername(String username);

       /**
        * 根据手机号查找申请
        */
       Optional<MerchantApplication> findByContactPhone(String contactPhone);

       /**
        * 根据手机号查找所有申请（包括历史记录）
        */
       List<MerchantApplication> findAllByContactPhone(String contactPhone);

       /**
        * 根据店铺名称查找申请
        */
       Optional<MerchantApplication> findByShopName(String shopName);

       /**
        * 根据店铺名称查找所有申请（包括历史记录）
        */
       List<MerchantApplication> findAllByShopName(String shopName);

       /**
        * 检查用户名是否存在
        */
       boolean existsByUsername(String username);

       /**
        * 检查手机号是否存在
        */
       boolean existsByContactPhone(String contactPhone);

       /**
        * 检查店铺名称是否存在
        */
       boolean existsByShopName(String shopName);

       /**
        * 根据审批状态查询
        */
       Page<MerchantApplication> findByApprovalStatus(Integer approvalStatus, Pageable pageable);

       /**
        * 根据审批状态和关键词查询
        */
       @Query("SELECT ma FROM MerchantApplication ma WHERE " +
                     "(:status IS NULL OR ma.approvalStatus = :status) AND " +
                     "(:keyword IS NULL OR ma.shopName LIKE %:keyword% OR " +
                     "ma.contactName LIKE %:keyword% OR ma.contactPhone LIKE %:keyword%)")
       Page<MerchantApplication> findByStatusAndKeyword(
                     @Param("status") Integer status,
                     @Param("keyword") String keyword,
                     Pageable pageable);

       /**
        * 统计指定状态的申请数量
        */
       long countByApprovalStatus(Integer approvalStatus);
}
