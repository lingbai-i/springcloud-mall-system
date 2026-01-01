package com.mall.common.core.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 时间工具类 - 统一使用北京时间（GMT+8）
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-01
 */
public class TimeUtils {
    
    /**
     * 北京时区（GMT+8）
     */
    public static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");
    
    /**
     * 默认日期时间格式
     */
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 默认日期格式
     */
    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 默认时间格式
     */
    public static final DateTimeFormatter DEFAULT_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("HH:mm:ss");
    
    /**
     * ISO 8601格式（带时区）
     */
    public static final DateTimeFormatter ISO_DATETIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    
    /**
     * 获取当前北京时间（ZonedDateTime）
     * 
     * @return 当前北京时间
     */
    public static ZonedDateTime now() {
        return ZonedDateTime.now(BEIJING_ZONE);
    }
    
    /**
     * 获取当前北京时间戳（毫秒）
     * 
     * @return 当前时间戳
     */
    public static long currentTimeMillis() {
        return Instant.now().toEpochMilli();
    }
    
    /**
     * 获取当前北京时间戳（秒）
     * 
     * @return 当前时间戳（秒）
     */
    public static long currentTimeSeconds() {
        return Instant.now().getEpochSecond();
    }
    
    /**
     * 获取当前北京时间的LocalDateTime
     * 
     * @return 当前北京时间
     */
    public static LocalDateTime nowLocalDateTime() {
        return LocalDateTime.now(BEIJING_ZONE);
    }
    
    /**
     * 获取当前北京日期
     * 
     * @return 当前日期
     */
    public static LocalDate nowLocalDate() {
        return LocalDate.now(BEIJING_ZONE);
    }
    
    /**
     * 格式化当前北京时间为字符串
     * 
     * @return 格式化后的时间字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static String nowString() {
        return nowLocalDateTime().format(DEFAULT_DATETIME_FORMATTER);
    }
    
    /**
     * 格式化当前北京日期为字符串
     * 
     * @return 格式化后的日期字符串（yyyy-MM-dd）
     */
    public static String nowDateString() {
        return nowLocalDate().format(DEFAULT_DATE_FORMATTER);
    }
    
    /**
     * 格式化LocalDateTime为字符串
     * 
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DEFAULT_DATETIME_FORMATTER);
    }
    
    /**
     * 格式化LocalDate为字符串
     * 
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DEFAULT_DATE_FORMATTER);
    }
    
    /**
     * 格式化ZonedDateTime为字符串
     * 
     * @param zonedDateTime 带时区的日期时间
     * @return 格式化后的字符串
     */
    public static String format(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.withZoneSameInstant(BEIJING_ZONE)
                .format(DEFAULT_DATETIME_FORMATTER);
    }
    
    /**
     * 解析日期时间字符串为LocalDateTime
     * 
     * @param dateTimeString 日期时间字符串（yyyy-MM-dd HH:mm:ss）
     * @return LocalDateTime对象
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, DEFAULT_DATETIME_FORMATTER);
    }
    
    /**
     * 解析日期字符串为LocalDate
     * 
     * @param dateString 日期字符串（yyyy-MM-dd）
     * @return LocalDate对象
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateString, DEFAULT_DATE_FORMATTER);
    }
    
    /**
     * 时间戳转LocalDateTime（北京时间）
     * 
     * @param timestamp 时间戳（毫秒）
     * @return LocalDateTime对象
     */
    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), BEIJING_ZONE);
    }
    
    /**
     * LocalDateTime转时间戳（毫秒）
     * 
     * @param dateTime 日期时间
     * @return 时间戳
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0L;
        }
        return dateTime.atZone(BEIJING_ZONE).toInstant().toEpochMilli();
    }
    
    /**
     * Date转LocalDateTime（北京时间）
     * 
     * @param date Date对象
     * @return LocalDateTime对象
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(BEIJING_ZONE).toLocalDateTime();
    }
    
    /**
     * LocalDateTime转Date
     * 
     * @param dateTime LocalDateTime对象
     * @return Date对象
     */
    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(BEIJING_ZONE).toInstant());
    }
    
    /**
     * 获取今天开始时间（00:00:00）
     * 
     * @return 今天开始时间
     */
    public static LocalDateTime todayStart() {
        return nowLocalDate().atStartOfDay();
    }
    
    /**
     * 获取今天结束时间（23:59:59）
     * 
     * @return 今天结束时间
     */
    public static LocalDateTime todayEnd() {
        return nowLocalDate().atTime(23, 59, 59, 999999999);
    }
    
    /**
     * 获取星期几（中文）
     * 
     * @param dateTime 日期时间
     * @return 星期几（星期一、星期二...）
     */
    public static String getWeekDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        return weekDays[dateTime.getDayOfWeek().getValue() % 7];
    }
    
    /**
     * 获取当前星期几（中文）
     * 
     * @return 星期几
     */
    public static String getCurrentWeekDay() {
        return getWeekDay(nowLocalDateTime());
    }
}

