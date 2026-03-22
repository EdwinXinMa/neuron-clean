package com.echarge.config.shiro;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 免Token认证注解
 * 
 * 认证系统结合spring MVC的@RequestMapping获取请求路径进行免登录配置
 * @author Edwin
 * @date 2026-03-22
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreAuth {
}
