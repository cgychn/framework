package com.framework.test;

import com.framework.annotation.AutoWired;
import com.framework.annotation.Service;
import com.framework.annotation.Transaction;

import java.util.List;

@Service
public class MyServiceImp4 implements MySupperService2 {

    @AutoWired
    TestMapper mapper;

    @Override
    public int test1(String aa) {
        System.out.println(aa);
        return 0;
    }

    @Override
    public int test2(int bb) {
        return 0;
    }

    @Override
    @Transaction
    public int testTx (String aa, String bb, int cc) {
        System.out.println(aa + bb + cc);

        List<User> userList = mapper.getUsers();
        System.out.println(userList);
        return 0;
    }
}
