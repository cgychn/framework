package com.framework.test;

import com.framework.annotation.AutoWired;
import com.framework.annotation.Service;
import com.framework.annotation.Transaction;

import java.util.List;

@Service
public class MySupperServiceImp1 implements MySupperService {

    @AutoWired
    TestMapper mapper;

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

    @Override
    @Transaction
    public int testTx(String aa, String bb, int cc) throws Exception {
        System.out.println(aa + bb + cc);

        List<User> userList = mapper.getUsers("a");
        System.out.println(userList);
        for (int i = 0; i < 20 ; i++) {
            mapper.addUser("asd" + i, "123456");
            if (i == 10) {
                throw new Exception("");
            }
        }

        return 0;
    }
}
