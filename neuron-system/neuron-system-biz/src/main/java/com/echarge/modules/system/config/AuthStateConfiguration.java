package com.echarge.modules.system.config;

import me.zhyd.oauth.cache.AuthStateCache;
import com.echarge.modules.system.cache.AuthStateRedisCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthStateConfiguration {

    @Bean
    public AuthStateCache authStateCache() {
        return new AuthStateRedisCache();
    }
}
