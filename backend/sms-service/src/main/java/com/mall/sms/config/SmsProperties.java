package com.mall.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信服务配置属性
 *
 * @author SMS Service
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {

    /**
     * 服务名称
     */
    private String serviceName = "baichatmall";

    /**
     * 验证码配置
     */
    private Code code = new Code();

    /**
     * 频率限制配置
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 第三方推送服务配置
     */
    private Push push = new Push();

    @Data
    public static class Code {
        /**
         * 验证码长度
         */
        private int length = 6;

        /**
         * 验证码过期时间（秒）
         */
        private int expireTime = 180;
    }

    @Data
    public static class RateLimit {
        /**
         * 同一手机号发送间隔（秒）
         */
        private int interval = 60;

        /**
         * 全局频率限制
         */
        private Global global = new Global();

        /**
         * IP频率限制
         */
        private Ip ip = new Ip();

        @Data
        public static class Global {
            /**
             * 全局每分钟最大请求数
             */
            private int maxRequests = 1000;

            /**
             * 时间窗口（秒）
             */
            private int timeWindow = 60;
        }

        @Data
        public static class Ip {
            /**
             * 单IP每分钟最大请求数
             */
            private int maxRequests = 20;

            /**
             * 时间窗口（秒）
             */
            private int timeWindow = 60;
        }
    }

    @Data
    public static class Push {
        /**
         * 推送服务URL
         */
        private String url = "https://push.spug.cc/send/zEMdom2N49mpgabP";

        /**
         * 连接超时（毫秒）
         */
        private int connectTimeout = 5000;

        /**
         * 读取超时（毫秒）
         */
        private int readTimeout = 10000;

        /**
         * 重试次数
         */
        private int retryTimes = 3;

        /**
         * 重试间隔（毫秒）
         */
        private int retryInterval = 1000;
    }
}