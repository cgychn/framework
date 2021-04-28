package com.framework.annotation.rpc;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCService {
    String name() default "";
    boolean useRemoteServiceProxy() default false;
    String provider() default "";
    String destRemoteImplClassName() default "";
}
