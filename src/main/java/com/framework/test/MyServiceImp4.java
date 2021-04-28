package com.framework.test;

import com.framework.annotation.framework.AutoWired;
import com.framework.annotation.framework.Service;
import com.framework.annotation.db.Transaction;

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

//        List<User> userList = mapper.getUsers();
//        System.out.println(userList);
        return 0;
    }
}
