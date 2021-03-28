package com.framework.test;

import com.framework.annotation.AutoWired;
import com.framework.annotation.Service;

@Service
public class MySupperServiceImp1 implements MySupperService {

    @AutoWired(name = "myServiceImp4")
    MySupperService2 mySupperService2;

    @Override
    public String test1(String aa) {
        mySupperService2.test1("this is aa");
        return "asdsa";
    }

    @Override
    public String test2(String bb) {
        return null;
    }
}
