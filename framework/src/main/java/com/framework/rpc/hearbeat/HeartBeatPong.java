package com.framework.rpc.hearbeat;

import java.io.Serializable;

public class HeartBeatPong implements Serializable {
    String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
