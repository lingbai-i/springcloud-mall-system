package com.mall.common.core.domain;

import com.mall.common.core.utils.TimeUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一返回结果
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
public class R<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 成功状态码 */
    public static final int SUCCESS = 200;
    
    /** 失败状态码 */
    public static final int FAIL = 500;
    
    /** 状态码 */
    private int code;
    
    /** 返回消息 */
    private String message;
    
    /** 返回数据 */
    private T data;
    
    /** 服务器时间戳（北京时间 GMT+8） */
    private long timestamp;
    
    public R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = TimeUtils.currentTimeMillis();
    }
    
    /**
     * 成功返回结果
     */
    public static <T> R<T> ok() {
        return new R<>(SUCCESS, "操作成功", null);
    }
    
    /**
     * 成功返回结果
     *
     * @param data 数据
     */
    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS, "操作成功", data);
    }
    
    /**
     * 成功返回结果
     *
     * @param message 消息
     * @param data 数据
     */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(SUCCESS, message, data);
    }
    
    /**
     * 失败返回结果
     */
    public static <T> R<T> fail() {
        return new R<>(FAIL, "操作失败", null);
    }
    
    /**
     * 失败返回结果
     *
     * @param message 消息
     */
    public static <T> R<T> fail(String message) {
        return new R<>(FAIL, message, null);
    }
    
    /**
     * 失败返回结果
     *
     * @param code 状态码
     * @param message 消息
     */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return SUCCESS == code;
    }
    
    /**
     * 判断是否失败
     */
    public boolean isFail() {
        return !isSuccess();
    }
    
    /**
     * 获取状态码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 获取消息
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 获取数据
     */
    public T getData() {
        return data;
    }
    
    /**
     * 获取时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
}