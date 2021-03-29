package com.framework.test;

import com.framework.annotation.Transaction;

public interface MySupperService {

    String test1(String aa);

    String test2(String bb);

    @Transaction
    int testTx(String aa, String bb, int cc) throws Exception;
}
