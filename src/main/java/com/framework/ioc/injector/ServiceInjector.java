package com.framework.ioc.injector;

import com.framework.annotation.framework.Service;
import com.framework.annotation.db.Transaction;
import com.framework.context.MyFrameworkContext;
import com.framework.transaction.TransactionInterceptor;

import java.lang.reflect.Method;

public class ServiceInjector implements Injector {

    static ServiceInjector serviceInjector = new ServiceInjector();

    public static Injector getInstance() {
        return serviceInjector;
    }

    @Override
    public void inject(Class cls) {
        if (cls.isAnnotationPresent(Service.class)) {
            // 托管对象
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
                if (((Service) cls.getDeclaredAnnotation(Service.class)).name().equals("")) {
                    // 代理事务
                    MyFrameworkContext.set(cls, po);
                } else {
                    MyFrameworkContext.set(cls, ((Service) cls.getDeclaredAnnotation(Service.class)).name(), po);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
