package com.framework.main;

import com.framework.config.MyFrameworkCfgContext;
import com.framework.context.MyClassLoader;
import com.framework.ioc.*;
import com.framework.rpc.server.RpcServer;

import java.io.IOException;
import java.util.List;

public class MyFrameworkRunner {

    public static void run (String scanPath, String frameWorkMainPropName) {

        // 加载所有的类
        List<Class> classes = MyClassLoader.loadClass(scanPath);
        // 注入容器
        classes.forEach(x -> {
            if (!x.isAnonymousClass() && !x.isMemberClass()) {
                // Starter
                StarterInjector.getInstance().inject(x);
                // DB Mapper
                MapperInjector.getInstance().inject(x);
                // Service
                ServiceInjector.getInstance().inject(x);
                // rpc Service
                RPCServiceInjector.getInstance().inject(x);
                // Configuration
                ConfigProviderInjector.getInstance().inject(x);
            }
        });

        // 加载框架的主配置文件
        try {
            MyFrameworkCfgContext.loadMainProp("application");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 装配
        ObjectAssembler.assemble();

        // 启动rpc服务
        try {
            RpcServer.startRPCServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void run (String scanPath) {
        run(scanPath, "application");
    }

}
