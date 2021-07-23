package com.main.main;

import com.framework.annotation.framework.FrameworkStarter;
import com.framework.context.MyFrameworkContext;
import com.framework.main.MyFrameworkRunner;
import com.main.entity.TestTable;
import com.main.rpc.service.TestService;

import java.util.List;

@FrameworkStarter
public class Main {

    public static void main(String[] args) {

        MyFrameworkRunner.run("com.main");

        TestService testService = MyFrameworkContext.getJustByClass(TestService.class);

        System.out.println(testService.test("aaa", 123));

        List<TestTable> lists = testService.testList();

        TestTable testTable = testService.testOne(0);

        System.out.println(lists);

        System.out.println(testTable);
    }

}
