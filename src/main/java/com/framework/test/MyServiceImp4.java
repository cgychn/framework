package com.framework.test;

import com.framework.annotation.Service;

@Service
public class MyServiceImp4 implements MySupperService2 {
    @Override
    public int test1(String aa) {
        System.out.println(aa);
        return 0;
    }

    @Override
    public int test2(int bb) {
        return 0;
    }
}
