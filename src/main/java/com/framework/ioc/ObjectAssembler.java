package com.framework.ioc;

import com.framework.annotation.AutoWired;
import com.framework.context.MyFrameworkContext;

import java.lang.reflect.Field;

public class ObjectAssembler {

    public static void assemble () {
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
    }

}
