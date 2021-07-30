package com.framework.main;

import com.framework.annotation.framework.FrameworkStarter;
import com.framework.config.MyFrameworkCfgContext;
import com.framework.context.MyClassLoader;
import com.framework.context.MyFrameworkContext;
import com.framework.ioc.ObjectAssembler;
import com.framework.ioc.injector.*;
import com.framework.rpc.client.ClientSocketHandlerPool;
import com.framework.rpc.register.RegisterSelector;
import com.framework.rpc.server.RpcServer;
import com.framework.util.ThreadPool;

import java.io.IOException;
import java.util.List;

public class MyFrameworkRunner {

    public static void run (String scanPath, String frameWorkMainPropName) {

        // 加载所有的类
        List<Class> classes = MyClassLoader.loadClass(scanPath);

        // 设置框架的启动类
        for (Class x : classes) {
            if (!x.isAnonymousClass()
                    && !x.isMemberClass()
                    && x.isAnnotationPresent(FrameworkStarter.class)) {
                StarterInjector.getInstance().inject(x);
                break;
            }
        }

        // 加载框架的主配置文件
        try {
            MyFrameworkCfgContext.loadMainProp("application");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 加载主框架线程池到上下文
        MyFrameworkContext.setFrameWorkThreadPool(ThreadPool.getInstance());
        MyFrameworkContext.setFrameWorkClientSocketPool(ClientSocketHandlerPool.getInstance());

        // 注入容器
        classes.forEach(x -> {
            if (!x.isAnonymousClass() && !x.isMemberClass()) {
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

        // 装配
        ObjectAssembler.assemble();

        // 先加载注册中心选择器
        try {
            Class.forName(RegisterSelector.class.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

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
