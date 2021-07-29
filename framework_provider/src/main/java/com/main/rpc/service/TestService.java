package com.main.rpc.service;


import com.main.entity.TestTable;

import java.util.List;

public interface TestService {

    public String test(String str, Integer aa);

    List<TestTable> testList();

    TestTable testOne(Integer id);

    List<TestTable> testSql();
}
