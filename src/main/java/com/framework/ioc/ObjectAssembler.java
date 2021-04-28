package com.framework.ioc;

import com.framework.annotation.AutoWired;
import com.framework.annotation.Value;
import com.framework.config.MyFrameworkCfgContext;
import com.framework.context.MyFrameworkContext;

import java.lang.reflect.Field;

public class ObjectAssembler {

    public static void assemble () {
        // 扫描容器，扫描被@Autowired注解的字段，并注入
        MyFrameworkContext.myContainer.forEach(x -> {
            Field[] fields = x.getType().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                // 注入容器中的对象
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

                // 注入配置文件中的对象
                if (field.isAnnotationPresent(Value.class)) {
                    String cfgValueKey = field.getDeclaredAnnotation(Value.class).value();
                    // 注入
                    try {
                        field.set(x.getObject(x.getType()), MyFrameworkCfgContext.get(cfgValueKey, x.getType()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
