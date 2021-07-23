package com.myrpc.service;

import com.framework.annotation.RPCService;

@RPCService
public class TestServiceImp implements TestService {
    @Override
    public String test(String str, Integer it) {
        return "hello";
    }
}
