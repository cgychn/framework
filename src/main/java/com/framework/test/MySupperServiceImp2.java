package com.framework.test;

import com.framework.annotation.framework.Service;

@Service
public class MySupperServiceImp2 implements MySupperService2 {
    @Override
    public int test1(String aa) {
        return 0;
    }

    @Override
    public int test2(int bb) {
        return 0;
    }

    @Override
    public int testTx(String aa, String bb, int cc) {
        return 0;
    }
}
