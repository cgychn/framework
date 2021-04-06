package com.framework.ioc;

import com.framework.annotation.Mapper;
import com.framework.annotation.Modifying;
import com.framework.annotation.Param;
import com.framework.annotation.Query;
import com.framework.context.MyFrameworkContext;
import com.framework.db.DBTool;
import com.framework.db.proxy.MapperProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;

public class MapperInjector implements Injector {

    static MapperInjector mapperInjector = new MapperInjector();

    private MapperInjector () {

    }

    public static MapperInjector getInstance() {
        return mapperInjector;
    }

    @Override
    public void inject(Class cls) {
        if (cls.isAnnotationPresent(Mapper.class)) {
            // 是mapper，添加到容器，mapper是数据库的映射器，是个接口，需要实现才能注入容器
            try {
                Object mapperInstance = new MapperProxy().bind(cls);
                if (((Mapper) cls.getDeclaredAnnotation(Mapper.class)).name().equals("")) {
                    MyFrameworkContext.set(
                            cls,
                            cls.cast(mapperInstance)
                    );
                } else {
                    MyFrameworkContext.set(
                            cls,
                            ((Mapper) cls.getDeclaredAnnotation(Mapper.class)).name(),
                            cls.cast(mapperInstance)
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
