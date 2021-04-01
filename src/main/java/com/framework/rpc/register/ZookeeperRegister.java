package com.framework.rpc.register;

import com.framework.rpc.register.entiy.RegisterClassEntity;
import com.framework.rpc.register.entiy.RegisterMethodEntity;
import com.framework.rpc.register.registry.ZookeeperRegistry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ZookeeperRegister implements Register {
    @Override
    public void registerToRegistry(Class cls) {
        Method[] methods = cls.getDeclaredMethods();
        // 向注册中心注册当前服务
        RegisterClassEntity registerClassEntity = new RegisterClassEntity();
        registerClassEntity.setCurrentClassName(cls.getName());
        registerClassEntity.setInterfaceName(
                String.join(
                        ",",
                        Arrays.asList(cls.getInterfaces())
                                .stream()
                                .map(x -> { return x.getName(); })
                                .collect(Collectors.toList())
                )
        );
        List<RegisterMethodEntity> registerMethodEntities = new ArrayList<>();
        for (Method method : methods) {
            RegisterMethodEntity registerMethodEntity = new RegisterMethodEntity();
            registerMethodEntity.setMethodName(method.getName());
            List<String> paramTypes = Arrays
                    .stream(method.getParameters())
                    .map(x -> { return x.getType().getName(); })
                    .collect(Collectors.toList());
            registerMethodEntity.setMethodArgs(method.getParameters().length > 0
                    ? paramTypes.toArray(new String[paramTypes.size()])
                    : new String[0]);
            registerMethodEntities.add(registerMethodEntity);
        }
        registerClassEntity.setMethodEntities(registerMethodEntities);

        System.out.println(registerClassEntity);
        ZookeeperRegistry.doRegister(registerClassEntity);
    }
}
