package com.sinloingok.app.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Utility class to access the Spring {@link ApplicationContext} statically.
 */
@Component
public class SBeanUtils implements ApplicationContextAware {
    private static ApplicationContext context;

    /**
     * Capture the application context for later retrieval.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SBeanUtils.context = applicationContext;
    }

    /**
     * Obtain a bean from the stored context by type.
     *
     * @param clazz bean class
     * @return bean instance
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }
}
