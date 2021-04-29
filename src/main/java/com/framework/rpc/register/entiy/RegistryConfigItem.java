package com.framework.rpc.register.entiy;

import com.framework.rpc.register.Registry;

public class RegistryConfigItem {

    String type;

    String ips;

    Integer timeout;

    Boolean justSubscribe;

    Boolean justRegister;

    String name;

    Registry registry;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIps() {
        return ips;
    }

    public void setIps(String ips) {
        this.ips = ips;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Boolean getJustSubscribe() {
        return justSubscribe;
    }

    public void setJustSubscribe(Boolean justSubscribe) {
        this.justSubscribe = justSubscribe;
    }

    public Boolean getJustRegister() {
        return justRegister;
    }

    public void setJustRegister(Boolean justRegister) {
        this.justRegister = justRegister;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
}
