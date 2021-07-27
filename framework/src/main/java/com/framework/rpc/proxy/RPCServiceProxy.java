package com.framework.rpc.proxy;

import com.framework.annotation.rpc.RPCService;
import com.framework.config.MyFrameworkCfgContext;
import com.framework.context.MyFrameworkContext;
import com.framework.rpc.client.ClientMessageHandler;
import com.framework.rpc.client.ClientSocketHandlerPool;
import com.framework.rpc.register.RegisterSelector;
import com.framework.rpc.register.entiy.RemoteClassEntity;

import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RPCServiceProxy implements InvocationHandler {

    String serviceName = null;
    String serviceProvider = null;
    String interfaceName = "";
    String destRemoteImplClassName = "";
    Boolean directConnect = false;
    String destRegistry = "";

    private static ClientSocketHandlerPool clientSocketPool = MyFrameworkContext.getClientSocketHandlerPool();

    public Object bind (Class cls) {
        // 获取远程服务的相应信息
        RPCService rpcService = (RPCService) cls.getDeclaredAnnotation(RPCService.class);

        // 获取该服务在prop中的配置
        String serviceProviderCfg = MyFrameworkCfgContext.get("framework.myrpc.consume." + rpcService.name() + ".provider", String.class);
        String destRemoteImplClassNameCfg = MyFrameworkCfgContext.get("framework.myrpc.consume." + rpcService.name() + ".destRemoteImplClassName", String.class);
        Boolean directConnect = MyFrameworkCfgContext.get("framework.myrpc.consume." + rpcService.directConnect() + ".directConnect", Boolean.class);
        String destRegistry = MyFrameworkCfgContext.get("framework.myrpc.consume." + rpcService.name() + ".destRegistry", String.class);

        this.serviceName = rpcService.name();
        this.interfaceName = cls.getName();

        // 如果该配置在配置文件中配置了，框架将会自动使用配置文件中的配置，否则使用注解中的配置
        this.serviceProvider = serviceProviderCfg == null ? rpcService.provider() : serviceProviderCfg;
        this.destRemoteImplClassName = destRemoteImplClassNameCfg == null ? rpcService.destRemoteImplClassName() : destRemoteImplClassNameCfg;
        this.directConnect = directConnect == null ? rpcService.directConnect() : directConnect;
        this.destRegistry = destRegistry == null ? rpcService.destRegistry() : destRegistry;

        return Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Boolean justReg = MyFrameworkCfgContext.get("framework.myrpc.provide.justRegister", Boolean.class);
        if (justReg != null && justReg == true) {
            // 配置了只注册，就不会再订阅注册中心服务
            System.out.println("该服务配置了只注册服务，不会订阅注册中心的服务，return null");
            return null;
        }

        // 获取当前的方法名称、参数、返回值
        String methodName = method.getName();
        Parameter[] methodParams = method.getParameters();
        Class[] methodParamTypes = new Class[methodParams.length];
        for (int i = 0; i < methodParams.length; i++) {
            methodParamTypes[i] = methodParams[i].getType();
        }

        RemoteClassEntity remoteClassEntity = null;
        if (this.directConnect) {
            // 直连，不通过注册中心找配置，直接向服务提供者索要配置

        } else {
            // 用上面获取到的值到注册中心找
            remoteClassEntity = RegisterSelector.findRemoteClassInEnabledRegistry(
                    this.serviceName,
                    this.serviceProvider.equals("") ? null : this.serviceProvider,
                    this.interfaceName,
                    this.destRemoteImplClassName.equals("") ? null : this.destRemoteImplClassName,
                    this.destRegistry.equals("") ? null : this.destRegistry
            );
        }

        // 用socket调用这个远程方法
        String provider = remoteClassEntity.getProvider();
        String className = remoteClassEntity.getImplClassName();
        String[] parts = provider.split(":");
        String ip = parts[0];
        Integer port = Integer.parseInt(parts[1]);

        AtomicReference<Object> val = new AtomicReference<>();

        // 通信服务提供者
        ClientMessageHandler handler = clientSocketPool.getSocketHandlerFromPool(ip, port);
        handler.sendMessage((outputStream) -> {
            try {
                // 读写流的循序：类名 -> 方法名 -> 参数类型 -> 参数值
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                // 写类名
                objectOutputStream.writeObject(className);
                // 写方法名
                objectOutputStream.writeObject(methodName);
                // 写方法参数类型
                objectOutputStream.writeObject(methodParamTypes);
                // 写方法参数值
                objectOutputStream.writeObject(args);
                objectOutputStream.flush();
                System.out.println(className + "," + methodName + "," + methodParamTypes + "," + args);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, (obj) -> {
            // 接受服务提供者返回
            val.set(obj);
            // 返还socket链接到连接池
            clientSocketPool.returnHandlerToPool(handler, ip, port);
            synchronized (val) {
                val.notify();
            }
        });

        // 等待结果返回
        synchronized (val) {
            val.wait();
        }

        // 转化对象并返回
        return method.getReturnType().cast(val.get());

    }
}
