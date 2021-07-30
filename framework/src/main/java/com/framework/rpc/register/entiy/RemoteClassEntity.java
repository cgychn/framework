package com.framework.rpc.register.entiy;

import java.util.ArrayList;
import java.util.List;

public class RemoteClassEntity {

    String provider;

    String implClassName;

    String interfaceName;

    List<RegisterMethodEntity> methodEntityList = new ArrayList<>();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getImplClassName() {
        return implClassName;
    }

    public void setImplClassName(String implClassName) {
        this.implClassName = implClassName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public List<RegisterMethodEntity> getMethodEntityList() {
        return methodEntityList;
    }

    public void setMethodEntityList(List<RegisterMethodEntity> methodEntityList) {
        this.methodEntityList = methodEntityList;
    }

    @Override
    public String toString() {
        return "RemoteClassEntity{" +
                "provider='" + provider + '\'' +
                ", implClassName='" + implClassName + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", methodEntityList=" + methodEntityList +
                '}';
    }
}
