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

        String res = testService.test("aaa", 0);

        new Thread(() -> {
            List<TestTable> l = testService.testList();
            System.out.println("多线程中输出1--- " + l);
        }).start();

        List<TestTable> lists = testService.testList();

        new Thread(() -> {
            List<TestTable> l = testService.testList();
            System.out.println("多线程中输出2--- " + l);
        }).start();

        TestTable testTable = testService.testOne(0);

        new Thread(() -> {
            TestTable l = testService.testOne(0);
            System.out.println("多线程中输出3--- " + l);
        }).start();
        new Thread(() -> {
            String l = testService.test("bbbwww", 0);
            System.out.println("多线程中输出4--- " + l);
        }).start();
        new Thread(() -> {
            List<TestTable> l = testService.testList();
            System.out.println("多线程中输出5--- " + l);
        }).start();

//        System.out.println(testService.test("aaa", 123));

        System.out.println("-----------" + res);

        System.out.println("-----------" + lists);

        System.out.println("-----------" + testTable);
    }

}
