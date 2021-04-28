package com.framework.test;

import com.framework.annotation.db.Transaction;

public interface MySupperService2 {

    int test1(String aa);

    int test2(int bb);

    @Transaction
    int testTx(String aa, String bb, int cc);
}
