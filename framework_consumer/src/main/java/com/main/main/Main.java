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

        long startTime = System.currentTimeMillis();
        String res = testService.test("aaa", 0);
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime));

        long startTime1 = System.currentTimeMillis();
        String res1 = testService.test("aaa", 0);
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime1));

        long startTime2 = System.currentTimeMillis();
        String res2 = testService.test("aaa", 0);
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime2));

//        new Thread(() -> {
//            long startTime1 = System.currentTimeMillis();
//            List<TestTable> l = testService.testList();
//            System.out.println("多线程中输出1--- " + l + "，耗时：" + (startTime1 - System.currentTimeMillis()));
//        }).start();

//        long startTime1 = System.currentTimeMillis();
//        List<TestTable> res1 = testService.testList();
//        System.out.println("耗时：" + (System.currentTimeMillis() - startTime1));

//        new Thread(() -> {
//            long startTime1 = System.currentTimeMillis();
//            List<TestTable> l = testService.testList();
//            System.out.println("多线程中输出2--- " + l + "，耗时：" + (startTime1 - System.currentTimeMillis()));
//        }).start();

//        long startTime2 = System.currentTimeMillis();
//        TestTable res2 = testService.testOne(0);
//        System.out.println("耗时：" + (System.currentTimeMillis() - startTime2));

//        new Thread(() -> {
//            long startTime1 = System.currentTimeMillis();
//            TestTable l = testService.testOne(0);
//            System.out.println("多线程中输出3--- " + l + "，耗时：" + (startTime1 - System.currentTimeMillis()));
//        }).start();
//        new Thread(() -> {
//            long startTime1 = System.currentTimeMillis();
//            String l = testService.test("bbbwww", 0);
//            System.out.println("多线程中输出4--- " + l + "，耗时：" + (startTime1 - System.currentTimeMillis()));
//        }).start();
//        new Thread(() -> {
//            long startTime1 = System.currentTimeMillis();
//            List<TestTable> l = testService.testList();
//            System.out.println("多线程中输出5--- " + l + "，耗时：" + (startTime1 - System.currentTimeMillis()));
//        }).start();

//        System.out.println(testService.test("aaa", 123));

        System.out.println("-----------" + res);

        System.out.println("-----------" + res1);

        System.out.println("-----------" + res2);
    }

}
