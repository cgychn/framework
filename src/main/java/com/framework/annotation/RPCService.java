package com.framework.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCService {
    String name() default "";
    boolean userRemoteServiceProxy() default false;
}
