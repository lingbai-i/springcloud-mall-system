-- MySQL dump 10.13  Distrib 8.0.17, for Win64 (x86_64)
--
-- Host: localhost    Database: mall_sms
-- ------------------------------------------------------
-- Server version	8.0.17

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `mall_sms`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `mall_sms` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `mall_sms`;

--
-- Table structure for table `sms_blacklist`
--

DROP TABLE IF EXISTS `sms_blacklist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sms_blacklist` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：phone-手机号，ip-IP地址',
  `value` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '值',
  `reason` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '加入黑名单原因',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间，NULL表示永久',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_value` (`type`,`value`),
  KEY `idx_type` (`type`),
  KEY `idx_value` (`value`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信黑名单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_blacklist`
--

LOCK TABLES `sms_blacklist` WRITE;
/*!40000 ALTER TABLE `sms_blacklist` DISABLE KEYS */;
/*!40000 ALTER TABLE `sms_blacklist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sms_log`
--

DROP TABLE IF EXISTS `sms_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sms_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '验证码',
  `purpose` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用途',
  `client_ip` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端IP',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0-失败，1-成功',
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误信息',
  `response` text COLLATE utf8mb4_unicode_ci COMMENT '第三方响应',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_phone_number` (`phone_number`),
  KEY `idx_client_ip` (`client_ip`),
  KEY `idx_purpose` (`purpose`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_log`
--

LOCK TABLES `sms_log` WRITE;
/*!40000 ALTER TABLE `sms_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sms_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sms_rate_limit`
--

DROP TABLE IF EXISTS `sms_rate_limit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sms_rate_limit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `mobile` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鎵嬫満鍙风爜',
  `purpose` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '楠岃瘉鐮佺敤閫',
  `request_count` int(11) NOT NULL DEFAULT '1' COMMENT '璇锋眰娆℃暟',
  `last_request_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鏈?悗璇锋眰鏃堕棿',
  `reset_time` datetime NOT NULL COMMENT '閲嶇疆鏃堕棿',
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '璇锋眰IP鍦板潃',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mobile_purpose` (`mobile`,`purpose`),
  KEY `idx_reset_time` (`reset_time`),
  KEY `idx_last_request_time` (`last_request_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鐭?俊棰戠巼闄愬埗璁板綍琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_rate_limit`
--

LOCK TABLES `sms_rate_limit` WRITE;
/*!40000 ALTER TABLE `sms_rate_limit` DISABLE KEYS */;
/*!40000 ALTER TABLE `sms_rate_limit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sms_send_record`
--

DROP TABLE IF EXISTS `sms_send_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sms_send_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `mobile` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鎵嬫満鍙风爜',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐭?俊鍐呭?',
  `service_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'baichatmall' COMMENT '鏈嶅姟鍚嶇О',
  `purpose` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍙戦?鐩?殑',
  `send_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '鍙戦?鐘舵?锛?-鍙戦?涓?紝1-鍙戦?鎴愬姛锛?-鍙戦?澶辫触',
  `response_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '绗?笁鏂瑰搷搴旂爜',
  `response_message` text COLLATE utf8mb4_unicode_ci COMMENT '绗?笁鏂瑰搷搴旀秷鎭',
  `cost_time` int(11) DEFAULT NULL COMMENT '鍙戦?鑰楁椂锛堟?绉掞級',
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '璇锋眰IP鍦板潃',
  `user_agent` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鐢ㄦ埛浠ｇ悊',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_mobile` (`mobile`),
  KEY `idx_send_status` (`send_status`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鐭?俊鍙戦?璁板綍琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_send_record`
--

LOCK TABLES `sms_send_record` WRITE;
/*!40000 ALTER TABLE `sms_send_record` DISABLE KEYS */;
INSERT INTO `sms_send_record` VALUES (1,'13800138000','Code:123456','baichatmall','LOGIN',1,'200','Success',1200,'192.168.1.100','Mozilla/5.0','2025-10-22 23:51:56','2025-10-22 23:51:56'),(2,'13800138001','Code:654321','baichatmall','REGISTER',1,'200','Success',1100,'192.168.1.101','Mozilla/5.0','2025-10-22 23:51:56','2025-10-22 23:51:56');
/*!40000 ALTER TABLE `sms_send_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sms_verification_code`
--

DROP TABLE IF EXISTS `sms_verification_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sms_verification_code` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `mobile` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鎵嬫満鍙风爜',
  `code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '楠岃瘉鐮',
  `service_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'baichatmall' COMMENT '鏈嶅姟鍚嶇О',
  `purpose` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '楠岃瘉鐮佺敤閫旓紙register/login/reset_password绛夛級',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '鐘舵?锛?-鏈?娇鐢?紝1-宸蹭娇鐢?紝2-宸茶繃鏈',
  `expire_time` datetime NOT NULL COMMENT '杩囨湡鏃堕棿',
  `used_time` datetime DEFAULT NULL COMMENT '浣跨敤鏃堕棿',
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '璇锋眰IP鍦板潃',
  `user_agent` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '鐢ㄦ埛浠ｇ悊',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_mobile_purpose` (`mobile`,`purpose`),
  KEY `idx_mobile_status` (`mobile`,`status`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='鐭?俊楠岃瘉鐮佽?褰曡〃';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_verification_code`
--

LOCK TABLES `sms_verification_code` WRITE;
/*!40000 ALTER TABLE `sms_verification_code` DISABLE KEYS */;
INSERT INTO `sms_verification_code` VALUES (1,'13800138000','123456','baichatmall','register',2,'2025-10-22 22:50:37',NULL,'127.0.0.1','Test-Agent','2025-10-22 23:50:37','2025-10-22 23:50:37'),(2,'13800138001','654321','baichatmall','login',2,'2025-10-22 23:20:37',NULL,'127.0.0.1','Test-Agent','2025-10-22 23:50:37','2025-10-22 23:50:37'),(3,'13800138000','123456','baichatmall','LOGIN',0,'2025-10-22 23:54:38',NULL,'192.168.1.100','Mozilla/5.0','2025-10-22 23:51:38','2025-10-22 23:51:38'),(4,'13800138001','654321','baichatmall','REGISTER',1,'2025-10-22 23:50:38',NULL,'192.168.1.101','Mozilla/5.0','2025-10-22 23:51:38','2025-10-22 23:51:38'),(5,'13800138000','123456','baichatmall','LOGIN',0,'2025-10-22 23:54:56',NULL,'192.168.1.100','Mozilla/5.0','2025-10-22 23:51:56','2025-10-22 23:51:56'),(6,'13800138001','654321','baichatmall','REGISTER',1,'2025-10-22 23:50:56',NULL,'192.168.1.101','Mozilla/5.0','2025-10-22 23:51:56','2025-10-22 23:51:56');
/*!40000 ALTER TABLE `sms_verification_code` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-26  9:26:33
