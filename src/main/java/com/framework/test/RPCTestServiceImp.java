package com.framework.test;

import com.framework.annotation.AutoWired;
import com.framework.annotation.RPCService;
import com.framework.annotation.Transaction;

@RPCService
public class RPCTestServiceImp implements RPCTestService {


    @AutoWired(name = "mySupperServiceImp1")
    MySupperService mySupperService;


    @Transaction
    @Override
    public void test1(String str1, String str2) throws Exception {
        System.out.println("test1 invoked in rpc service");
        System.out.println(str1 + " " + str2);
        mySupperService.testTx(str1, str2, 3);
    }

    @Override
    public void test2(String str1, String str2) {

    }

    @Override
    public Integer test1(String str1, String str2, Integer int1) {
        return null;
    }
}
