package com.framework.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestThreadLocal {


    ThreadLocal<List> threadLocal = new ThreadLocal<>();

    public void printThreadLocal () {

        if (threadLocal.get() == null) {
            List list = new ArrayList();
            list.add(Thread.currentThread().getName());
            threadLocal.set(list);
        } else {
            threadLocal.get().add(Thread.currentThread().getName());
        }
        try {
            System.out.println(threadLocal.get());
            Thread.sleep(new Random().nextInt(20));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
