package com.myrpc.service;

import com.framework.annotation.RPCService;

@RPCService(name = "testService")
public interface TestService {

    public String test(String str, Integer it);

}
