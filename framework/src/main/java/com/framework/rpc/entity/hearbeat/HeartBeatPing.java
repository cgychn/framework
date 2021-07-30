package com.framework.rpc.entity.hearbeat;

import java.io.Serializable;

public class HeartBeatPing implements Serializable {
    String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
