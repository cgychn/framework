package com.framework.main;


import com.alibaba.fastjson.JSON;
import com.framework.annotation.*;
import com.framework.context.IocEntity;
import com.framework.context.MyFrameworkContext;
import com.framework.context.TransactionInterceptor;
import com.framework.test.MySupperServiceImp1;
import com.framework.util.DBTool;
import com.framework.util.MyClassLoader;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main (String[] args) throws Exception {

        // 加载所有的类
        List<Class> classes = MyClassLoader.loadClass("com.framework");
        classes.forEach(x -> {
            if (!x.isAnonymousClass() && !x.isMemberClass()) {
                if (x.isAnnotationPresent(Service.class)) {
                    // 托管对象
                    try {
                        Object po = null;
                        Method[] methods = x.getDeclaredMethods();
                        boolean hasTransactionAnnotation = false;
                        for (Method method : methods) {
                            if (method.isAnnotationPresent(Transaction.class)) {
                                // 代理改方法
                                hasTransactionAnnotation = true;
                                break;
                            }
                        }
                        if (hasTransactionAnnotation) {
                            po = new TransactionInterceptor().getInstance(x.newInstance());
                        } else {
                            po = x.newInstance();
                        }
                        if (((Service) x.getDeclaredAnnotation(Service.class)).name().equals("")) {
                            // 代理事务
                            MyFrameworkContext.set(x, po);
                        } else {
                            MyFrameworkContext.set(x, ((Service) x.getDeclaredAnnotation(Service.class)).name(), po);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (x.isAnnotationPresent(Mapper.class)) {
                    // 是mapper，添加到容器，mapper是数据库的映射器，是个接口，需要实现才能注入容器
                    Object mapperInstance = Proxy.newProxyInstance(x.getClassLoader(), new Class[]{x}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            // 是 insert/update
                            Query query = method.getDeclaredAnnotation(Query.class);
                            String sql = query.sql();
                            // 用args 匹配sql中的占位符
                            Parameter[] parameters = method.getParameters();
                            // 构造一个 参数：值 的结构
                            for (int i = 0 ; i < parameters.length ; i++) {
                                sql = sql.replace("#{" + parameters[i].getAnnotation(Param.class).value() + "}", "'" + args[i].toString() + "'");
                            }
                            System.out.println(sql);
                            if (method.isAnnotationPresent(Modifying.class)) {
                                return DBTool.update(sql);
                            } else {
                                return DBTool.query(sql, method.getReturnType());
                            }
                        }
                    });
                    if (((Mapper) x.getDeclaredAnnotation(Mapper.class)).name().equals("")) {
                        MyFrameworkContext.set(
                                x,
                                x.cast(mapperInstance)
                        );
                    } else {
                        MyFrameworkContext.set(
                                x,
                                ((Mapper) x.getDeclaredAnnotation(Mapper.class)).name(),
                                x.cast(mapperInstance)
                        );
                    }
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
        mySupperServiceImp1.testTx("cc", "vv", 22);
    }

}
