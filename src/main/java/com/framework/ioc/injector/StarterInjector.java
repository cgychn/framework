package com.framework.ioc.injector;

import com.framework.context.MyFrameworkContext;

public class StarterInjector implements Injector {

    static StarterInjector starterInjector = new StarterInjector();

    public static StarterInjector getInstance () {
        return starterInjector;
    }

    @Override
    public void inject(Class cls) {
        MyFrameworkContext.setMainClass(cls);
    }
}
