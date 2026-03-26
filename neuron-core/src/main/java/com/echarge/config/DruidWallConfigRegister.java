package com.echarge.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 启动程序修改DruidWallConfig配置
 * 允许SELECT语句的WHERE子句是一个永真条件
 * @author Edwin
 * @date 2026-03-22
 */
public class DruidWallConfigRegister implements SpringApplicationRunListener {

    private final SpringApplication application;

    /**
     * 必备，否则启动报错
     * @param application Spring 应用
     * @param args 启动参数
     */
    public DruidWallConfigRegister(SpringApplication application, String[] args) {
        this.application = application;
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        ConfigurableEnvironment env = context.getEnvironment();
        Map<String, Object> props = new HashMap<>(2);
        props.put("spring.datasource.dynamic.druid.wall.selectWhereAlwayTrueCheck", false);

        MutablePropertySources propertySources = env.getPropertySources();

        PropertySource<Map<String, Object>> propertySource = new MapPropertySource("neuron-datasource-config", props);

        propertySources.addLast(propertySource);
    }
}