package com.mall.admin.repository;

import com.mall.admin.domain.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 管理员数据访问层接口
 * 提供管理员相关的数据库操作方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    /**
     * 根据用户名查找管理员
     * 
     * @param username 用户名
     * @return 管理员信息
     */
    Optional<Admin> findByUsername(String username);
    
    /**
     * 根据邮箱查找管理员
     * 
     * @param email 邮箱
     * @return 管理员信息
     */
    Optional<Admin> findByEmail(String email);
    
    /**
     * 根据手机号查找管理员
     * 
     * @param phone 手机号
     * @return 管理员信息
     */
    Optional<Admin> findByPhone(String phone);
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);
    
    /**
     * 根据状态查找管理员列表
     * 
     * @param status 状态
     * @return 管理员列表
     */
    List<Admin> findByStatus(Integer status);
    
    /**
     * 根据角色查找管理员列表
     * 
     * @param role 角色
     * @return 管理员列表
     */
    List<Admin> findByRole(String role);
    
    /**
     * 查找指定时间范围内创建的管理员
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 管理员列表
     */
    @Query("SELECT a FROM Admin a WHERE a.createTime BETWEEN :startTime AND :endTime")
    List<Admin> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找指定时间范围内最后登录的管理员
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 管理员列表
     */
    @Query("SELECT a FROM Admin a WHERE a.lastLoginTime BETWEEN :startTime AND :endTime")
    List<Admin> findByLastLoginTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计管理员总数
     * 
     * @return 管理员总数
     */
    @Query("SELECT COUNT(a) FROM Admin a")
    Long countTotalAdmins();
    
    /**
     * 统计活跃管理员数量
     * 
     * @param status 活跃状态
     * @return 活跃管理员数量
     */
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.status = :status")
    Long countByStatus(@Param("status") Integer status);
    
    /**
     * 统计指定时间范围内新增的管理员数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新增管理员数量
     */
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.createTime BETWEEN :startTime AND :endTime")
    Long countByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                 @Param("endTime") LocalDateTime