package com.framework.rpc.register.registry;

import com.framework.rpc.register.entiy.RegisterClassEntity;

public class ZookeeperRegistry {

    // 这些将来都会抽离到配置文件中

    public static String serviceName = "testService";

    public static String ip = "10.0.0.23";

    public static String port = "9098";

    public static void doRegister (RegisterClassEntity registerClassEntity) {
        //
    }

    public static void unRegister () {

    }
}
