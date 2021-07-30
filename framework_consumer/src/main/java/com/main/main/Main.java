package com.main.main;

import com.framework.annotation.framework.FrameworkStarter;
import com.framework.context.MyFrameworkContext;
import com.framework.main.MyFrameworkRunner;
import com.main.entity.TestTable;
import com.main.rpc.service.TestService;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@FrameworkStarter
public class Main {

    public static void main(String[] args) throws InterruptedException {

        MyFrameworkRunner.run("com.main");

        TestService testService = MyFrameworkContext.getJustByClass(TestService.class);

        long startTime = System.currentTimeMillis();
        String res = testService.test("aaa", 0);
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime));
//
//        long startTime1 = System.currentTimeMillis();
//        String res1 = testService.test("aaa", 0);
//        System.out.println("耗时：" + (System.currentTimeMillis() - startTime1));
//
//        long startTime2 = System.currentTimeMillis();
//        String res2 = testService.test("aaa", 0);
//        System.out.println("耗时：" + (System.currentTimeMillis() - startTime2));

//        List<TestTable> aa = testService.testSql();
//
//        testService.testee();
//
//        System.out.println(aa);

//        new Thread(() -> {
//            long startTime1 = System.currentTimeMillis();
//            List<TestTable> l = testService.testList();
//            System.out.println("多线程中输出1--- " + l + "，耗时：" + (startTime1 - System.currentTimeMillis()));
//        }).start();

        long startTime1 = System.currentTimeMillis();
        List<TestTable> res1 = testService.testList();
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime1));

//        new Thread(() -> {
//            long startTime1 = System.currentTimeMillis();
//            List<TestTable> l = testService.testList();
//            System.out.println("多线程中输出2--- " + l + "，耗时：" + (startTime1 - System.currentTimeMillis()));
//        }).start();

        long startTime2 = System.currentTimeMillis();
        TestTable res2 = testService.testOne(0);
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime2));

        CountDownLatch countDownLatch = new CountDownLatch(9);

        new Thread(() -> {
            try {
                long startTime11 = System.currentTimeMillis();
                TestTable l = testService.testOne(0);
                System.out.println("多线程中输出3--- " + l + "，耗时：" + (startTime11 - System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            try {
                long startTime12 = System.currentTimeMillis();
                String l = testService.test("bbbwww", 0);
                System.out.println("多线程中输出4--- " + l + "，耗时：" + (startTime12 - System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            try {
                long startTime13 = System.currentTimeMillis();
                List<TestTable> l = testService.testList();
                System.out.println("多线程中输出5--- " + l + "，耗时：" + (startTime13 - System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            try {
                long startTime11 = System.currentTimeMillis();
                TestTable l = testService.testOne(0);
                System.out.println("多线程中输出6--- " + l + "，耗时：" + (startTime11 - System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            try {
                long startTime12 = System.currentTimeMillis();
                String l = testService.test("bbbwww", 0);
                System.out.println("多线程中输出7--- " + l + "，耗时：" + (startTime12 - System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            try {
                long startTime13 = System.currentTimeMillis();
                List<TestTable> l = testService.testList();
                System.out.println("多线程中输出8--- " + l + "，耗时：" + (startTime13 - System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            try {
                long startTime11 = System.currentTimeMillis();
                TestTable l = testService.testOne(0);
                System.out.println("多线程中输出9--- " + l + "，耗时：" + (startTime11 - System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            try {
                long startTime12 = System.currentTimeMillis();
                String l = testService.test("bbbwww", 0);
                System.out.println("多线程中输出10--- " + l + "，耗时：" + (startTime12 - System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            try {
                long startTime13 = System.currentTimeMillis();
                List<TestTable> l = testService.testList();
                System.out.println("多线程中输出11--- " + l + "，耗时：" + (startTime13 - System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }).start();


        System.out.println(testService.test("aaa", 123));

        System.out.println("-----------" + res);

        System.out.println("-----------" + res1);

        System.out.println("-----------" + res2);

        countDownLatch.await();

//        Thread.sleep(100000);
    }

}
