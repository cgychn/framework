package com.framework.annotation.db;

import java.lang.annotation.*;

/**
 * 被注解的mapper，启用缓存
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableCache {
    /**
     * 命名空间，如果不指定，命名空间则为类名
     * @return
     */
    String nameSpace() default "";

    /**
     * 超时时间，如果不设置超时时间，默认为永不过时
     * @return
     */
    long timeOut() default -1L;

    /**
     * 默认的cache实现类为 com.framework.db.cache.LocalCache，如果有额外需求，请实现 Cache 借口
     * @return
     */
    String cacheImplClass() default "com.framework.db.cache.LocalCache";
}
