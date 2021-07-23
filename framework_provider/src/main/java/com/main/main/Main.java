package com.main.main;


import com.framework.annotation.framework.FrameworkStarter;
import com.framework.main.MyFrameworkRunner;

@FrameworkStarter
public class Main {

    public static void main(String[] args) {
        // 指定扫描路径
        MyFrameworkRunner.run("com.main");
    }

}
