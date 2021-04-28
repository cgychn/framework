package com.framework.ioc;

import com.framework.annotation.FrameworkStarter;
import com.framework.context.MyFrameworkContext;

public class StarterInjector implements Injector {

    static StarterInjector starterInjector = new StarterInjector();

    public static StarterInjector getInstance () {
        return starterInjector;
    }

    @Override
    public void inject(Class cls) {
        if (cls.isAnnotationPresent(FrameworkStarter.class)) {
            MyFrameworkContext.setMainClass(cls);
        }
    }
}
