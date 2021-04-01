package com.framework.rpc.register.entiy;

import java.util.List;

public class RegisterClassEntity {

    String interfaceName;

    String currentClassName;

    List<RegisterMethodEntity> methodEntities;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getCurrentClassName() {
        return currentClassName;
    }

    public void setCurrentClassName(String currentClassName) {
        this.currentClassName = currentClassName;
    }

    public List<RegisterMethodEntity> getMethodEntities() {
        return methodEntities;
    }

    public void setMethodEntities(List<RegisterMethodEntity> methodEntities) {
        this.methodEntities = methodEntities;
    }

}
