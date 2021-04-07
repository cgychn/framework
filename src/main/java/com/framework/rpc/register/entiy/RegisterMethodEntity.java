package com.framework.rpc.register.entiy;

import java.util.Arrays;

public class RegisterMethodEntity {

    String methodName;

    String methodArgs[];

    String returnType;

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

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return "RegisterMethodEntity{" +
                "methodName='" + methodName + '\'' +
                ", methodArgs=" + Arrays.toString(methodArgs) +
                ", returnType='" + returnType + '\'' +
                '}';
    }
}
