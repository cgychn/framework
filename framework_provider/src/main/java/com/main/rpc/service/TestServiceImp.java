package com.main.rpc.service;

import com.framework.annotation.framework.AutoWired;
import com.framework.annotation.rpc.RPCService;
import com.main.entity.TestTable;
import com.main.mapper.TestOneMapper;

import java.util.List;

@RPCService
public class TestServiceImp implements TestService {

    @AutoWired
    public TestOneMapper testOneMapper;

    @Override
    public String test (String str, Integer aa) {
        List<TestTable> testTableList = testOneMapper.getResult(0);
        List<TestTable> testTableList1 = testOneMapper.getResult(0);
        System.out.println("test List 1 : " + testTableList);
        System.out.println("test List 2 : " + testTableList1);
        return "hello, i received the message " + str + " " + aa;
    }

    @Override
    public List<TestTable> testList() {
        List<TestTable> testTableList = testOneMapper.getResult(0);
        return testTableList;
    }

    @Override
    public TestTable testOne(Integer id) {
        List<TestTable> testTableList = testOneMapper.getResult(id);
        return testTableList.get(0);
    }

}
