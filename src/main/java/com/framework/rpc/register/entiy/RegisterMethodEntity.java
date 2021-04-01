package com.framework.rpc.register.entiy;

import java.util.Arrays;

public class RegisterMethodEntity {

    String methodName;

    String methodArgs[];

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getMethodArgs() {
        return methodArgs;
    }

    public void setMethodArgs(String[] methodArgs) {
        this.methodArgs = methodArgs;
    }

    @Override
    public String toString() {
        return "MethodEntity{" +
                "methodName='" + methodName + '\'' +
                ", methodArgs=" + Arrays.toString(methodArgs) +
                '}';
    }

}
