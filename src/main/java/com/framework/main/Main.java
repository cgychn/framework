package com.framework.main;


public class Main {

    public static void main (String[] args) throws Exception {

        MyFrameworkRunner.run();

        // test
//        RPCTestServiceImp mySupperServiceImp1 = MyFrameworkContext.get(RPCTestServiceImp.class);
//
//        Method method = RPCTestServiceImp.class.getMethod("test1", new Class[]{String.class, String.class});
//        method.invoke(mySupperServiceImp1, new Object[]{"asd", "fds"});
//        mySupperServiceImp1.test1("asd", "fds");
//        mySupperServiceImp1.testTx("cc", "vv", 22);
    }

}
