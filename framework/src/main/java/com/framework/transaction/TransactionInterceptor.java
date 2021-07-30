package com.framework.transaction;

import com.framework.annotation.db.Transaction;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class TransactionInterceptor implements MethodInterceptor {

    private Object o;

    public Object getInstance (Object o) {
        this.o = o;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(this.o.getClass());
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (method.isAnnotationPresent(Transaction.class)) {
            System.out.println("into transaction invoke");
            // 为该线程的改方法创建一个事务，在该方法中的sql都将使用该事务
            try {
                // 开启事务（为当前的线程创建一个connection对象，取消connection的autocommit属性）
                TransactionManager.createATransaction();
                // 执行方法体
                Object res = methodProxy.invokeSuper(o, args);
                // 提交事务
                TransactionManager.commit();
                return res;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                try {
                    // 出错回滚
                    System.out.println("role back");
                    TransactionManager.rollback();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            } finally {
                // 在返回之前必须关闭事务，不然可能会内存泄漏
                TransactionManager.closeConnection();
                System.out.println("connection closed");
            }
        } else {
            // 直接代理
            return methodProxy.invokeSuper(o, args);
        }
    }
}
