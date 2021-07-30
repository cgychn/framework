package com.framework.annotation.db;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapper {
    String name () default "";
    String nameSpace () default "";
}
