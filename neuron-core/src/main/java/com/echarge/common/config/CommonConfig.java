/*
 * 
 * Could not load the following classes:
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package com.echarge.common.config;

import com.echarge.common.util.SpringContextHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {
    @Bean
    @ConditionalOnMissingBean(value={SpringContextHolder.class})
    public SpringContextHolder springContextHolder() {
        SpringContextHolder holder = new SpringContextHolder();
        return holder;
    }
}

