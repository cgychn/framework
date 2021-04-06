package com.framework.rpc.register.registry;

import com.framework.rpc.register.entiy.RegisterClassEntity;

public interface Registry {
    void doRegister(RegisterClassEntity registerClassEntity) throws Exception;
}
