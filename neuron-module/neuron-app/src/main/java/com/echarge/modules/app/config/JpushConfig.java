package com.echarge.modules.app.config;

import cn.jiguang.common.ClientConfig;
import cn.jpush.api.JPushClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 极光推送初始化
 */
@Slf4j
@Configuration
public class JpushConfig {

    @Value("${jpush.app-key}")
    private String appKey;

    @Value("${jpush.master-secret}")
    private String masterSecret;

    @Bean
    public JPushClient jpushClient() {
        JPushClient client = new JPushClient(masterSecret, appKey, null, ClientConfig.getInstance());
        log.info("[JPush] 极光推送客户端初始化成功");
        return client;
    }
}
