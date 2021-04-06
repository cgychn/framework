package com.framework.ioc;

import com.framework.annotation.RPCService;
import com.framework.annotation.Transaction;
import com.framework.context.MyFrameworkContext;
import com.framework.rpc.register.ZookeeperRegister;
import com.framework.transaction.TransactionInterceptor;
import com.framework.util.StringUtil;

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
                if (!rpcService.userRemoteServiceProxy() && !cls.isInterface()) {
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

                    // 注册
                    ZookeeperRegister.getInstance().registerToRegistry(cls);
                } else {
                    // 说明该类是一个远程类

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
