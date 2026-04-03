package com.echarge.modules.app.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * App Token 过滤器注册
 * @author Edwin
 */
@Configuration
public class AppFilterConfig {

    @Bean
    public FilterRegistrationBean<AppTokenFilter> appTokenFilterRegistration(AppTokenFilter filter) {
        FilterRegistrationBean<AppTokenFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/app/*");
        registration.setOrder(1);
        registration.setName("appTokenFilter");
        return registration;
    }
}
