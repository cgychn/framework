package com.framework.annotation.framework;

import java.lang.annotation.*;

/**
 * 启动器，被标注的类为启动类，框架会从这个类上获取classloader
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FrameworkStarter {
}
