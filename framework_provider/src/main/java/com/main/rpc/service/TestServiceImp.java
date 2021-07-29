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
        long startTime1 = System.currentTimeMillis();
        List<TestTable> testTableList = testOneMapper.getResult(0);
        List<TestTable> testTableList1 = testOneMapper.getResult(0);
        System.out.println("test List 1 : " + testTableList);
        System.out.println("test List 2 : " + testTableList1);
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime1));
        return "hello, i received the message " + str + " " + aa;
    }

    @Override
    public List<TestTable> testList() {
        long startTime1 = System.currentTimeMillis();
        List<TestTable> testTableList = testOneMapper.getResult(0);
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime1));
        return testTableList;
    }

    @Override
    public TestTable testOne(Integer id) {
        long startTime1 = System.currentTimeMillis();
        List<TestTable> testTableList = testOneMapper.getResult(id);
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime1));
        return testTableList.get(0);
    }

    @Override
    public List<TestTable> testSql() {
        long startTime1 = System.currentTimeMillis();
        List<TestTable> testTableList = testOneMapper.getRes(0, 10, 10, 1);
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime1));
        return testTableList;
    }

}
