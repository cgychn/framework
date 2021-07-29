package com.main.rpc.service;

import com.framework.annotation.rpc.RPCService;
import com.main.entity.TestTable;

import java.util.List;

@RPCService(name = "testService", destRegistry = "test1")
public interface TestService {

    public String test(String str, Integer aa);

    List<TestTable> testList();

    TestTable testOne(Integer id);

    List<TestTable> testSql();

    public void testee ();

}
