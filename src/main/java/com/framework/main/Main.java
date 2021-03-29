package com.framework.main;


import com.framework.context.MyFrameworkContext;
import com.framework.ioc.MapperInjector;
import com.framework.ioc.ObjectAssembler;
import com.framework.ioc.ServiceInjector;
import com.framework.test.MySupperServiceImp1;
import com.framework.context.MyClassLoader;

import java.util.List;

public class Main {

    public static void main (String[] args) throws Exception {

        // 加载所有的类
        List<Class> classes = MyClassLoader.loadClass("com.framework");
        // 注入容器
        classes.forEach(x -> {
            if (!x.isAnonymousClass() && !x.isMemberClass()) {
                new MapperInjector().inject(x);
                new ServiceInjector().inject(x);
            }
        });
        // 装配
        ObjectAssembler.assemble();


        // test
        MySupperServiceImp1 mySupperServiceImp1 = MyFrameworkContext.get(MySupperServiceImp1.class);
        mySupperServiceImp1.test1("bb");
        mySupperServiceImp1.testTx("cc", "vv", 22);
    }

}
