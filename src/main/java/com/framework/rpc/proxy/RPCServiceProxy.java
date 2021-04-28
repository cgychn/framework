package com.framework.rpc.proxy;

import com.framework.annotation.rpc.RPCService;
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
        this.serviceName = rpcService.name();
        this.serviceProvider = rpcService.provider();
        this.interfaceName = cls.getName();
        this.destRemoteImplClassName = rpcService.destRemoteImplClassName();
        return Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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
