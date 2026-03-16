/*
 * 
 * Could not load the following classes:
 *  org.springframework.core.annotation.AliasFor
 *  org.springframework.stereotype.Component
 */
package com.echarge.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

@Documented
@Inherited
@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
@Component
public @interface RabbitComponent {
    @AliasFor(annotation=Component.class)
    public String value();
}

