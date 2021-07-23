package com.framework.test;

public class Test {

    public static void main (String[] args) {

        TestThreadLocal testThreadLocal = new TestThreadLocal();
        for (int i = 0; i < 10000; i++) {
            int finalI = i;
            new Thread(() -> {
                testThreadLocal.printThreadLocal();
                if (finalI % 10 == 0) {
                    testThreadLocal.printThreadLocal();
                }
            }).start();
        }

    }

}
