package com.framework.main;


import com.alibaba.fastjson.JSON;
import com.framework.annotation.AutoWired;
import com.framework.annotation.Mapper;
import com.framework.annotation.Service;
import com.framework.context.IocEntity;
import com.framework.context.MyFrameworkContext;
import com.framework.test.MySupperServiceImp1;
import com.framework.util.MyClassLoader;

import java.lang.reflect.Field;
import java.util.List;

public class Main {

    public static void main (String[] args) throws Exception {

        // 加载所有的类
        List<Class> classes = MyClassLoader.loadClass("com.framework");
        classes.forEach(x -> {
            if (!x.isAnonymousClass() && !x.isMemberClass()) {
                if (x.isAnnotationPresent(Service.class)) {
                    // 托管对象
                    try {
                        if (((Service) x.getDeclaredAnnotation(Service.class)).name().equals("")) {
                            MyFrameworkContext.set(x, x.newInstance());
                        } else {
                            MyFrameworkContext.set(x, ((Service) x.getDeclaredAnnotation(Service.class)).name(), x.newInstance());
                        }
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                if (x.isAnnotationPresent(Mapper.class)) {
                    // 是mapper，添加到容器，mapper是数据库的映射器，是个接口，需要实现才能注入容器
                    
                }
            }
        });
        // 扫描容器，扫描被@Autowired注解的字段，并注入
        MyFrameworkContext.myContainer.forEach(x -> {
            Field[] fields = x.getType().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(AutoWired.class)) {
                    // 注入
                    Object object;
                    if (field.getDeclaredAnnotation(AutoWired.class).name().equals("")) {
                        object = MyFrameworkContext.get(field.getType());
                    } else {
                        object = MyFrameworkContext.get(field.getType(), field.getDeclaredAnnotation(AutoWired.class).name());
                    }
                    try {
                        field.set(x.getObject(x.getType()), object);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // test
        MySupperServiceImp1 mySupperServiceImp1 = MyFrameworkContext.get(MySupperServiceImp1.class);
        mySupperServiceImp1.test1("bb");
    }

}
