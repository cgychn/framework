package com.framework.annotation;

import java.lang.annotation.*;

/**
 * 配置提供者，将配置文件中的配置读成类属性
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CfgProvider {

    String cfgFileName() default "";

}
