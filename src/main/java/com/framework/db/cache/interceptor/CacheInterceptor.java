package com.framework.db.cache.interceptor;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CacheInterceptor implements MethodInterceptor {

    private Object o;

    public Object getInstance (Object o) {
        this.o = o;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(this.o.getClass());
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        return null;
    }
}
