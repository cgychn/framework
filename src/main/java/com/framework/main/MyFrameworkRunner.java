package com.framework.main;

import com.framework.context.MyClassLoader;
import com.framework.ioc.MapperInjector;
import com.framework.ioc.ObjectAssembler;
import com.framework.ioc.RPCServiceInjector;
import com.framework.ioc.ServiceInjector;
import com.framework.rpc.server.RpcServer;

import java.io.IOException;
import java.util.List;

public class MyFrameworkRunner {

    public static void run () {
        // 加载所有的类
        List<Class> classes = MyClassLoader.loadClass("com.framework");
        // 注入容器
        classes.forEach(x -> {
            if (!x.isAnonymousClass() && !x.isMemberClass()) {
                MapperInjector.getInstance().inject(x);
                ServiceInjector.getInstance().inject(x);
                RPCServiceInjector.getInstance().inject(x);
            }
        });
        // 装配
        ObjectAssembler.assemble();

        // 启动rpc服务
        try {
            RpcServer.startRPCServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
