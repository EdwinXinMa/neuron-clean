/*
 * 
 * Could not load the following classes:
 *  cn.hutool.core.util.ObjectUtil
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.context.ApplicationContext
 *  org.springframework.context.ApplicationContextAware
 *  org.springframework.context.annotation.Lazy
 *  org.springframework.stereotype.Component
 */
package com.echarge.common.util;

import cn.hutool.core.util.ObjectUtil;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy(value=false)
@Component
public class SpringContextHolder
implements ApplicationContextAware {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(SpringContextHolder.class);
    private static ApplicationContext applicationContext;
    private static boolean isWarningLogged;

    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolder.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        SpringContextHolder.checkApplicationContext();
        return applicationContext;
    }

    public static <T> T getBean(String name) {
        SpringContextHolder.checkApplicationContext();
        return (T)applicationContext.getBean(name);
    }

    public static <T> T getHandler(String name, Class<T> cls) {
        Object t;
        block3: {
            t = null;
            if (ObjectUtil.isNotEmpty(name)) {
                SpringContextHolder.checkApplicationContext();
                try {
                    t = applicationContext.getBean(name, cls);
                }
                catch (Exception e) {
                    if (isWarningLogged) break block3;
                    log.warn("Customize redis listener handle [ " + name + " ], does not exist\uff01");
                    isWarningLogged = true;
                }
            }
        }
        return (T)t;
    }

    public static <T> T getBean(Class<T> clazz) {
        SpringContextHolder.checkApplicationContext();
        return (T)applicationContext.getBean(clazz);
    }

    public static void cleanApplicationContext() {
        applicationContext = null;
    }

    private static void checkApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("applicationContext\u672a\u6ce8\u5165\uff0c\u8bf7\u5728applicationContext.xml\u4e2d\u5b9a\u4e49SpringContextHolder");
        }
    }

    static {
        isWarningLogged = false;
    }
}

