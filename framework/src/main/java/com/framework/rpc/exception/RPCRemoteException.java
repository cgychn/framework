package com.framework.rpc.exception;

import java.io.Serializable;

public class RPCRemoteException extends Exception implements Serializable {

    /**
     * 该字段不可以手动修改
     */
    boolean serverException;

    public RPCRemoteException (String msg) {
        super(msg);
    }

    public Boolean getServerException () {
        return serverException;
    }

    public void setServerException (boolean serverException) {
        this.serverException = serverException;
    }

}
