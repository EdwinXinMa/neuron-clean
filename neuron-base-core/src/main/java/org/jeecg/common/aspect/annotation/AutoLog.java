package org.jeecg.common.aspect.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLog {
    String value() default "";
    int logType() default 2;
    int operateType() default 0;
    com.echarge.common.constant.enums.ModuleType module() default com.echarge.common.constant.enums.ModuleType.COMMON;
}
