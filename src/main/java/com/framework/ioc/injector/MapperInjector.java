package com.framework.ioc.injector;

import com.framework.annotation.db.Mapper;
import com.framework.context.MyFrameworkContext;
import com.framework.db.proxy.MapperProxy;

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
