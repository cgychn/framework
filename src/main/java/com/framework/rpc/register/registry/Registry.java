package com.framework.rpc.register.registry;

import com.framework.rpc.register.entiy.RegisterClassEntity;
import com.framework.rpc.register.entiy.RemoteClassEntity;

public interface Registry {
    void doRegister(RegisterClassEntity registerClassEntity) throws Exception;
    // 知道远程服务的名称，并知道接口的名称，并知道接口实现的名称，并指定服务提供者（不指定调用者由客户端实现负载均衡）
    RemoteClassEntity findRemoteClass(String serviceName, String provider, String interfaceName, String implClassName) throws Exception;
    // 知道远程服务的名称，并知道接口的名称，并知道接口实现的名称（一个接口多实现）
    RemoteClassEntity findRemoteClass(String serviceName, String interfaceName, String implClassName) throws Exception;
    // 知道远程服务的名称，并知道接口的名称（就能拿到接口的实现，前提是接口的实现有且只有一个）
    RemoteClassEntity findRemoteClass(String serviceName, String interfaceName) throws Exception;
}
