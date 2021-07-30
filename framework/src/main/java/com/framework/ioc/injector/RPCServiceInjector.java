package com.framework.ioc.injector;

import com.framework.annotation.rpc.RPCService;
import com.framework.annotation.db.Transaction;
import com.framework.context.MyFrameworkContext;
import com.framework.rpc.proxy.RPCServiceProxy;
import com.framework.rpc.register.RegisterSelector;
import com.framework.rpc.register.zookeeper.ZookeeperRegister;
import com.framework.transaction.TransactionInterceptor;

import java.lang.reflect.Method;

public class RPCServiceInjector implements Injector {

    static RPCServiceInjector rpcServiceInjector = new RPCServiceInjector();

    public static Injector getInstance() {
        return rpcServiceInjector;
    }

    @Override
    public void inject(Class cls) {
        // 被RPCService注解了，说明是远程调用，将该类的相关信息插入到注册中心
        if (cls.isAnnotationPresent(RPCService.class)) {
            RPCService rpcService = ((RPCService) cls.getDeclaredAnnotation(RPCService.class));
            try {
                // 如果该服务是被注解类的提供者
                if (!rpcService.useRemoteServiceProxy() && !cls.isInterface()) {
                    Object po = null;
                    Method[] methods = cls.getDeclaredMethods();
                    boolean hasTransactionAnnotation = false;
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(Transaction.class)) {
                            // 代理改方法
                            hasTransactionAnnotation = true;
                            break;
                        }
                    }
                    if (hasTransactionAnnotation) {
                        po = new TransactionInterceptor().getInstance(cls.newInstance());
                    } else {
                        po = cls.newInstance();
                    }
                    if (rpcService.name().equals("")) {
                        // 代理事务
                        MyFrameworkContext.set(cls, po);
                    } else {
                        MyFrameworkContext.set(cls, rpcService.name(), po);
                    }

                    // 注册，这里应该接受配置，让用户选择使用哪个注册中心
                    RegisterSelector.registerServiceToEnabledRegistry(cls);
//                    ZookeeperRegister.getInstance().registerToRegistry(cls);
                } else {
                    // 说明该类是一个远程类，获取该类的调用信息，并生成代理
                    Object rpcInstance = new RPCServiceProxy().bind(cls);
                    // 注入容器
                    MyFrameworkContext.set(cls, rpcInstance);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
