package org.jeecg.common.aspect.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dict {
    String dicCode();
    String dicText() default "";
    String dictTable() default "";
    String ds() default "";
}
