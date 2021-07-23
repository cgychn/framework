package com.consumer.test;

import com.framework.context.MyFrameworkContext;
import com.framework.main.MyFrameworkRunner;
import com.myrpc.service.TestService;

public class Main {

    public static void main(String[] args) {
        MyFrameworkRunner.run("com.myrpc.service");

        TestService testService = MyFrameworkContext.getJustByClass(TestService.class);

        System.out.println(testService.test("你好", 10));

    }

}
