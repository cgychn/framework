package com.main.rpc.service;

import com.framework.annotation.rpc.RPCService;

@RPCService
public class Test2ServiceImp implements Test2Service {



    @Override
    public void test (String arg) {
        System.out.println("收到参数：" + arg);
    }

}
