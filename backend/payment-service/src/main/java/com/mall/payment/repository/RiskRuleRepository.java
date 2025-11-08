package com.mall.payment.repository;

import com.mall.payment.entity.RiskRule;
import com.mall.payment.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 风控规则数据访问接口
 * 提供风控规则的数据库操作方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Repository
public interface RiskRuleRepository extends JpaRepository<RiskRule, String> {

    /**
     * 查询启用的风控规则，按优先级排序
     * 
     * @return 启用的风控规则列表
     */
    @Query("SELECT r FROM RiskRule r WHERE r.enabled = true ORDER BY r.priority ASC, r.createdAt ASC")
    List<RiskRule> findEnabledRulesOrderByPriority();

    /**
     * 根据规则类型查询启用的风控规则
     * 
     * @param ruleType 规则类型
     * @return 启用的风控规则列表
     */
    @Query("SELECT r FROM RiskRule r WHERE r.enabled = true AND r.ruleType = :ruleType ORDER BY r.priority ASC")
    List<RiskRule> findEnabledRulesByType(@Param("ruleType") RiskRule.RuleType ruleType);

    /**
     * 根据支付方式查询适用的启用风控规则
     * 
     * @param paymentMethod 支付方式
     * @return 适用的启用风控规则列表
     */
    @Query("SELECT r FROM RiskRule r WHERE r.enabled = true AND (r.paymentMethod IS NULL OR r.paymentMethod = :paymentMethod) ORDER BY r.priority ASC")
    List<RiskRule> findEnabledRulesByPaymentMethod(@Param("paymentMethod") PaymentMethod paymentMethod);

    /**
     * 根据规则类型和支付方式查询适用的启用风控规则
     * 
     * @param ruleType 规则类型
     * @param paymentMethod 支付方式
     * @return 适用的启用风控规则列表
     */
    @Query("SELECT r FROM RiskRule r WHERE r.enabled = true AND r.ruleType = :ruleType AND (r.paymentMethod IS NULL OR r.paymentMethod = :paymentMethod) ORDER BY r.priority ASC")
    List<RiskRule> findEnabledRulesByTypeAndPaymentMethod(@Param("ruleType") RiskRule.RuleType ruleType, 
                                                          @Param("paymentMethod") PaymentMethod paymentMethod);

    /**
     * 根据风险等级查询启用的风控规则
     * 
     * @param riskLevel 风险等级
     * @return 启用的风控规则列表
     */
    @Query("SELECT r FROM RiskRule r WHERE r.enabled = true AND r.riskLevel = :riskLevel ORDER BY r.priority ASC")
    List<RiskRule> findEnabledRulesByRiskLevel(@Param("riskLevel") RiskRule.RiskLevel riskLevel);

    /**
     * 根据处理动作查询启用的风控规则
     * 
     * @param action 处理动作
     * @return 启用的风控规则列表
     */
    @Query("SELECT r FROM RiskRule r WHERE r.enabled = true AND r.action = :action ORDER BY r.priority ASC")
    List<RiskRule> findEnabledRulesByAction(@Param("action") RiskRule.RiskAction action);

    /**
     * 分页查询风控规则
     * 
     * @param pageable 分页参数
     * @return 风控规则分页结果
     */
    Page<RiskRule> findAll(Pageable pageable);

    /**
     * 根据规则名称模糊查询风控规则
     * 
     * @param ruleName 规则名称关键字
     * @param pageable 分页参数
     * @return 风控规则分页结果
     */
    @Query("SELECT r FROM RiskRule r WHERE r.ruleName LIKE %:ruleName% ORDER BY r.priority ASC")
    Page<RiskRule> findByRuleNameContaining(@Param("ruleName") String ruleName, Pageable pageable);

    /**
     * 根据规则类型分页查询风控规则
     * 
     * @param ruleType 规则类型
     * @param pageable 分页参数
     * @return 风控规则分页结果
     */
    Page<RiskRule> findByRuleType(RiskRule.RuleType ruleType, Pageable pageable);

    /**
     * 根据启用状态分页查询风控规则
     * 
     * @param enabled 启用状态
     * @param pageable 分页参数
     * @return 风控规则分页结果
     */
    Page<RiskRule> findByEnabled(Boolean enabled, Pageable pageable);

    /**
     * 根据支付方式分页查询风控规则
     * 
     * @param paymentMethod 支付方式
     * @param pageable 分页参数
     * @return 风控规则分页结果
     */
    Page<RiskRule> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);

    /**
     * 根据风险等级分页查询风控规则
     * 
     * @param riskLevel 风险等级
     * @param pageable 分页参数
     * @return 风控规则分页结果
     */
    Page<RiskRule> findByRiskLevel(RiskRule.RiskLevel riskLevel, Pageable pageable);

    /**
     * 检查规则名称是否已存在
     * 
     * @param ruleName 规则名称
     * @return 是否存在
     */
    boolean existsByRuleName(String ruleName);

    /**
     * 检查规则名称是否已存在（排除指定ID）
     * 
     * @param ruleName 规则名称
     * @param ruleId 排除的规则ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(r) > 0 FROM RiskRule r WHERE r.ruleName = :ruleName AND r.ruleId != :ruleId")
    boolean existsByRuleNameAndRuleIdNot(@Param("ruleName") String ruleName, @Param("ruleId") String ruleId);

    /**
     * 统计启用的风控规则数量
     * 
     * @return 启用的风控规则数量
     */
    @Query("SELECT COUNT(r) FROM RiskRule r WHERE r.enabled = true")
    long countEnabledRules();

    /**
     * 根据规则类型统计启用的风控规则数量
     * 
     * @param ruleType 规则类型
     * @return 启用的风控规则数量
     */
    @Query("SELECT COUNT(r) FROM RiskRule r WHERE r.enabled = true AND r.ruleType = :ruleType")
    long countEnabledRulesByType(@Param("ruleType") RiskRule.RuleType ruleType);

    /**
     * 根据风险等级统计启用的风控规则数量
     * 
     * @param riskLevel 风险等级
     * @return 启用的风控规则数量
     */
    @Query("SELECT COUNT(r) FROM RiskRule r WHERE r.enabled = true AND r.riskLevel = :riskLevel")
    long countEnabledRulesByRiskLevel(@Param("riskLevel") RiskRule.RiskLevel riskLevel);

    /**
     * 查询最大优先级值
     * 
     * @return 最大优先级值
     */
    @Query("SELECT COALESCE(MAX(r.priority), 0) FROM RiskRule r")
    Integer findMaxPriority();

    /**
     * 查询指定优先级范围内的规则数量
     * 
     * @param minPriority 最小优先级
     * @param maxPriority 最大优先级
     * @return 规则数量
     */
    @Query("SELECT COUNT(r) FROM RiskRule r WHERE r.priority BETWEEN :minPriority AND :maxPriority")
    long countByPriorityBetween(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);
}