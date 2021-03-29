package com.framework.context;

import com.framework.ioc.IocEntity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MyFrameworkContext {

    public static Set<IocEntity> myContainer = new HashSet<>();

    public static void set (Class cls, Object obj) {
        String[] clsParts = cls.getName().split("\\.");
        set(cls, firstCharLowerCase(clsParts[clsParts.length - 1]), obj);
    }

    public static void set (Class cls, String name, Object obj) {
        IocEntity iocEntity = new IocEntity();
        iocEntity.setName(name);
        iocEntity.setObject(obj);
        iocEntity.setType(cls);
        myContainer.add(iocEntity);
    }

    public static <T> T get (Class<T> cls) {
        return get(cls, firstCharLowerCase(cls.getSimpleName()));
    }

    public static <T> T get (Class<T> cls, String name) {
//        System.out.println("cls :" + cls);
//        System.out.println("name :" + name);
        for (IocEntity iocEntity : myContainer) {
//            System.out.println("ioc type :" + iocEntity.getType());
            if (
                    (cls == iocEntity.getType() && name.equals(iocEntity.getName()))
                    || (
                            Arrays.stream(iocEntity.getType().getInterfaces()).filter(x -> { return x == cls; }).findAny().isPresent()
                                    && firstCharLowerCase(iocEntity.getType().getSimpleName()).equals(name)
                    )
            ) {
                return (T) iocEntity.getObject(iocEntity.getType());
            }
        }
        return null;
    }

    private static String firstCharLowerCase (String val) {
        char[] chars = val.toCharArray();
        chars[0] = (chars[0] + "").toLowerCase().toCharArray()[0];
        return String.valueOf(chars);
    }


}
