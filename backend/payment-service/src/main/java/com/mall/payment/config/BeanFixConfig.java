package com.mall.payment.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Configuration;

/**
 * Bean修复配置
 * 解决ddlApplicationRunner的NullBean问题
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-11-11
 */
@Configuration
public class BeanFixConfig implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 移除有问题的ddlApplicationRunner bean定义
        try {
            if (beanFactory instanceof BeanDefinitionRegistry) {
                BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                if (registry.containsBeanDefinition("ddlApplicationRunner")) {
                    registry.removeBeanDefinition("ddlApplicationRunner");
                }
            }
        } catch (Exception e) {
            // 忽略移除失败的情况
        }
    }
}
