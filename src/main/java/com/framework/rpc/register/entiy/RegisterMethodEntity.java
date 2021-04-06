package com.framework.rpc.register.entiy;

import java.util.Arrays;

public class RegisterMethodEntity {

    String methodName;

    String methodArgs[];

    Object returnType;

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

    public Object getReturnType() {
        return returnType;
    }

    public void setReturnType(Object returnType) {
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return "MethodEntity{" +
                "methodName='" + methodName + '\'' +
                ", methodArgs=" + Arrays.toString(methodArgs) +
                '}';
    }

}
