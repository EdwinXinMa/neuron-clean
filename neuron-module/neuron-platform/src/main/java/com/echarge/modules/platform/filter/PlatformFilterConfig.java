package com.echarge.modules.platform.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 平台 API Key 过滤器注册
 *
 * @author Edwin
 */
@Configuration
public class PlatformFilterConfig {

    @Bean
    public FilterRegistrationBean<PlatformApiKeyFilter> platformApiKeyFilterRegistration(PlatformApiKeyFilter filter) {
        FilterRegistrationBean<PlatformApiKeyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/platform/*");
        registration.setOrder(2);
        registration.setName("platformApiKeyFilter");
        return registration;
    }
}
