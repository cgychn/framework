package com.framework.rpc.proxy;

import com.framework.annotation.rpc.RPCService;
import com.framework.config.MyFrameworkCfgContext;
import com.framework.rpc.register.entiy.RemoteClassEntity;
import com.framework.rpc.register.zookeeper.ZookeeperRegistry;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.net.Socket;

public class RPCServiceProxy implements InvocationHandler {

    String serviceName = null;
    String serviceProvider = null;
    String interfaceName = "";
    String destRemoteImplClassName = "";

    public Object bind (Class cls) {
        // 获取远程服务的相应信息
        RPCService rpcService = (RPCService) cls.getDeclaredAnnotation(RPCService.class);

        // 获取该服务在prop中的配置
        String serviceProviderCfg = MyFrameworkCfgContext.get("framework.myrpc.consume." + rpcService.name() + ".provider", String.class);
        String destRemoteImplClassNameCfg = MyFrameworkCfgContext.get("framework.myrpc.consume." + rpcService.name() + ".destRemoteImplClassName", String.class);

        this.serviceName = rpcService.name();
        this.interfaceName = cls.getName();

        // 如果该配置在配置文件中配置了，框架将会自动使用配置文件中的配置，否则使用注解中的配置
        this.serviceProvider = serviceProviderCfg == null ? rpcService.provider() : serviceProviderCfg;
        this.destRemoteImplClassName = destRemoteImplClassNameCfg == null ? rpcService.destRemoteImplClassName() : destRemoteImplClassNameCfg;

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
        Object[] methodArgs = args;
        Class[] methodParamTypes = new Class[methodParams.length];
        for (int i = 0; i < methodParams.length; i++) {
            methodParamTypes[i] = methodParams[i].getType();
        }
        // 用上面获取到的值到注册中心找
        RemoteClassEntity remoteClassEntity = ZookeeperRegistry.getInstance().findRemoteClass(
                this.serviceName,
                this.serviceProvider.equals("") ? null : this.serviceProvider,
                this.interfaceName,
                this.destRemoteImplClassName.equals("") ? null : this.destRemoteImplClassName
        );
        // 用socket调用这个远程方法
        String provider = remoteClassEntity.getProvider();
        String[] parts = provider.split(":");
        String ip = parts[0];
        Integer port = Integer.parseInt(parts[1]);
        Socket socket = new Socket(ip, port);
        OutputStream outputStream = socket.getOutputStream();
        // 读写流的循序：类名 -> 方法名 -> 参数类型 -> 参数值
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(remoteClassEntity.getImplClassName());
        objectOutputStream.writeObject(methodName);
        objectOutputStream.writeObject(methodParamTypes);
        objectOutputStream.writeObject(args);
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        return method.getReturnType().cast(objectInputStream.readObject());
    }
}
