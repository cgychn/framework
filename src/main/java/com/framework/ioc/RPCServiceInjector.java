package com.framework.ioc;

import com.framework.annotation.RPCService;
import com.framework.annotation.Transaction;
import com.framework.context.MyFrameworkContext;
import com.framework.rpc.register.ZookeeperRegister;
import com.framework.rpc.register.entiy.RegisterClassEntity;
import com.framework.rpc.register.entiy.RegisterMethodEntity;
import com.framework.transaction.TransactionInterceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RPCServiceInjector implements Injector {


    @Override
    public void inject(Class cls) {
        // 被RPCService注解了，说明是远程调用，将该类的相关信息插入到注册中心
        if (cls.isAnnotationPresent(RPCService.class)) {
            try {
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
                if (((RPCService) cls.getDeclaredAnnotation(RPCService.class)).name().equals("")) {
                    // 代理事务
                    MyFrameworkContext.set(cls, po);
                } else {
                    MyFrameworkContext.set(cls, ((RPCService) cls.getDeclaredAnnotation(RPCService.class)).name(), po);
                }

                // 注册
                new ZookeeperRegister().registerToRegistry(cls);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
