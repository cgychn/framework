package com.framework.db.cache;

/**
 * 缓存的数据原子
 */
public class CacheBean {

    String sql;
    Long timeOut;
    Object value;
    String nameSpace;
    Long timeOutTimeStamp;

    public Long getTimeOutTimeStamp() {
        return timeOutTimeStamp;
    }

    public void setTimeOutTimeStamp(Long timeOutTimeStamp) {
        this.timeOutTimeStamp = timeOutTimeStamp;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Long timeOut) {
        this.timeOut = timeOut;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }
}
