package org.jeecg.common.aspect.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface PermissionData {
    String value() default "";
    String pageComponent() default "";
}
