package com.framework.context;

import com.framework.ioc.IocEntity;
import com.framework.rpc.client.ClientSocketHandlerPool;
import com.framework.util.StringUtil;
import com.framework.util.ThreadPool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MyFrameworkContext {

    public static Set<IocEntity> myContainer = new HashSet<>();

    public static Class mainClass = null;

    private static ThreadPool threadPool;

    private static ClientSocketHandlerPool clientSocketPool;

    public static void set (Class cls, Object obj) throws Exception {
        String[] clsParts = cls.getName().split("\\.");
        set(cls, StringUtil.firstCharLowerCase(clsParts[clsParts.length - 1]), obj);
    }

    public static void set (Class cls, String name, Object obj) throws Exception {
        // set之前先检查
        if (checkBeanName(name)) {
            IocEntity iocEntity = new IocEntity();
            iocEntity.setName(name);
            iocEntity.setObject(obj);
            iocEntity.setType(cls);
            myContainer.add(iocEntity);
        } else {
            throw new Exception("entity name repeat");
        }
    }

    private static boolean checkBeanName (String name) {
        for (IocEntity iocEntity : myContainer) {
            if (iocEntity.getName().equals(name)) {
                // 重复
                return false;
            }
        }
        return true;
    }

    public static <T> T get (Class<T> cls) {
        return get(cls, StringUtil.firstCharLowerCase(cls.getSimpleName()));
    }

    /**
     * 仅根据类获取相应的实例
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T getJustByClass (Class<T> cls) {
        for (IocEntity iocEntity : myContainer) {
            if (cls == iocEntity.getType()) {
                return (T) iocEntity.getObject(iocEntity.getType());
            }
        }
        return null;
    }

    public static <T> T get (Class<T> cls, String name) {
        for (IocEntity iocEntity : myContainer) {
            if (
                    (cls == iocEntity.getType() && name.equals(iocEntity.getName()))
                    || (
                            Arrays.stream(iocEntity.getType().getInterfaces()).filter(x -> { return x == cls; }).findAny().isPresent()
                                    && StringUtil.firstCharLowerCase(iocEntity.getType().getSimpleName()).equals(name)
                    )
            ) {
                return (T) iocEntity.getObject(iocEntity.getType());
            }
        }
        return null;
    }

    public static void setMainClass (Class cls) {
        System.out.println(cls);
        mainClass = cls;
    }

    public static Class getMainClass () {
        return mainClass;
    }


    public static void setFrameWorkThreadPool(ThreadPool threadPool) {
        MyFrameworkContext.threadPool = threadPool;
    }

    public static ThreadPool getFrameWorkThreadPool() {
        return threadPool;
    }

    public static void setFrameWorkClientSocketPool(ClientSocketHandlerPool clientSocketPool) {
        MyFrameworkContext.clientSocketPool = clientSocketPool;
    }

    public static ClientSocketHandlerPool getClientSocketHandlerPool () {
        return clientSocketPool;
    }
}
