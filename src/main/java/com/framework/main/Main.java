package com.framework.main;


import com.framework.context.MyFrameworkContext;
import com.framework.ioc.MapperInjector;
import com.framework.ioc.ObjectAssembler;
import com.framework.ioc.RPCServiceInjector;
import com.framework.ioc.ServiceInjector;
import com.framework.test.MySupperServiceImp1;
import com.framework.context.MyClassLoader;
import com.framework.test.RPCTestServiceImp;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
                new RPCServiceInjector().inject(x);
            }
        });
        // 装配
        ObjectAssembler.assemble();


        // test
        RPCTestServiceImp mySupperServiceImp1 = MyFrameworkContext.get(RPCTestServiceImp.class);

        Method method = RPCTestServiceImp.class.getMethod("test1", new Class[]{String.class, String.class});
        method.invoke(mySupperServiceImp1, new Object[]{"asd", "fds"});
//        mySupperServiceImp1.test1("asd", "fds");
//        mySupperServiceImp1.testTx("cc", "vv", 22);
    }

}
