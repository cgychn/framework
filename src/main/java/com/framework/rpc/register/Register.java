package com.framework.rpc.register;

import com.framework.rpc.register.entiy.RegisterClassEntity;

public interface Register {
    /**
     * 注册的类
     * @param cls
     */
    void registerToRegistry (Class cls);

}
