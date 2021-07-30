package com.framework.ioc;

public class IocEntity {

    Class type;

    String name;

    Object object;

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public <T> T getObject(Class<T> cls) {
        return (T) object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return type.getName() + "||" + name + "||" + object.toString();
    }
}
