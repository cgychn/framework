package com.framework.rpc.register.zookeeper;

import com.framework.rpc.register.Register;
import com.framework.rpc.register.entiy.RegisterClassEntity;
import com.framework.rpc.register.entiy.RegisterMethodEntity;
import com.framework.rpc.register.entiy.RegistryConfigItem;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ZookeeperRegister implements Register {

    static ZookeeperRegister zookeeperRegister = new ZookeeperRegister();

    private ZookeeperRegister () {

    }

    public static ZookeeperRegister getInstance() {
        return zookeeperRegister;
    }

    @Override
    public void registerToRegistry(Class cls, RegistryConfigItem registryConfigItem) {
        Method[] methods = cls.getDeclaredMethods();
        // 向注册中心注册当前服务
        RegisterClassEntity registerClassEntity = new RegisterClassEntity();
        registerClassEntity.setCurrentClassName(cls.getName());
        // 这个类只能实现一个接口
        registerClassEntity.setInterfaceName(
                Arrays.asList(cls.getInterfaces())
                        .stream()
                        .map(x -> { return x.getName(); }).collect(Collectors.toList()).get(0)
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
            registerMethodEntity.setReturnType(method.getReturnType().getName());
            registerMethodEntities.add(registerMethodEntity);
        }
        registerClassEntity.setMethodEntities(registerMethodEntities);

        System.out.println(registerClassEntity);

        try {
            registryConfigItem.getRegistry().doRegister(registerClassEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
