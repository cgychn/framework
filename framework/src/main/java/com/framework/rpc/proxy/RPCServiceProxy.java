package com.framework.rpc.proxy;

import com.framework.annotation.rpc.RPCService;
import com.framework.config.MyFrameworkCfgContext;
import com.framework.context.MyFrameworkContext;
import com.framework.rpc.client.ClientMessageHandler;
import com.framework.rpc.client.ClientSocketHandlerPool;
import com.framework.rpc.register.RegisterSelector;
import com.framework.rpc.register.entiy.RemoteClassEntity;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
        long startTime = System.currentTimeMillis();
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
            // 用上面获取到的值到注册中心找（这里比较卡，原因是要访问zookeeper读取节点信息，这里加缓存）
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
        AtomicBoolean callbackReturned = new AtomicBoolean(false);
        // 通信服务提供者
        ClientMessageHandler handler = clientSocketPool.getSocketHandlerFromPool(ip, port);
        Object[] msg = {className, methodName, methodParamTypes, args};

        System.out.println("invoke时间：" + (System.currentTimeMillis() - startTime));
        long sendTime = System.currentTimeMillis();
        handler.sendMessage(msg, (obj) -> {
            // 接受服务提供者返回
            val.set(obj);
            // 返还socket链接到连接池
            clientSocketPool.returnHandlerToPool(handler, ip, port);
            synchronized (callbackReturned) {
                callbackReturned.set(true);
                callbackReturned.notify();
            }
        });

        // 等待结果返回
        synchronized (callbackReturned) {
            if (!callbackReturned.get()) {
                callbackReturned.wait();
            }
        }
        System.out.println("调用时间：" + (System.currentTimeMillis() - sendTime));
        // 转化对象并返回
        return method.getReturnType().cast(val.get());

    }
}
