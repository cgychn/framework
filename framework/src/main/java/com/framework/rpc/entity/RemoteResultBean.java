package com.framework.rpc.entity;

import com.framework.rpc.entity.hearbeat.HeartBeatPing;
import com.framework.rpc.entity.hearbeat.HeartBeatPong;
import com.framework.rpc.exception.RPCRemoteException;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 远端实体包装类，防止产生歧义
 */
public class RemoteResultBean implements Serializable {

    /**
     * 心跳检测
     */
    private HeartBeatPing heartBeatPing;
    /**
     * 心跳回应
     */
    private HeartBeatPong heartBeatPong;
    /**
     * 响应内容
     */
    private Object[] objs;
    /**
     * 远端异常
     */
    private RPCRemoteException rpcRemoteException;

    public HeartBeatPing getHeartBeatPing() {
        return heartBeatPing;
    }

    public void setHeartBeatPing(HeartBeatPing heartBeatPing) {
        this.heartBeatPing = heartBeatPing;
    }

    public HeartBeatPong getHeartBeatPong() {
        return heartBeatPong;
    }

    public void setHeartBeatPong(HeartBeatPong heartBeatPong) {
        this.heartBeatPong = heartBeatPong;
    }

    public Object[] getObjs() {
        return objs;
    }

    public void setObjs(Object[] objs) {
        this.objs = objs;
    }

    public RPCRemoteException getRpcRemoteException() {
        return rpcRemoteException;
    }

    public void setRpcRemoteException(RPCRemoteException rpcRemoteException) {
        this.rpcRemoteException = rpcRemoteException;
    }

    @Override
    public String toString() {
        return "RemoteResultBean{" +
                "heartBeatPing=" + heartBeatPing +
                ", heartBeatPong=" + heartBeatPong +
                ", objs=" + Arrays.toString(objs) +
                ", rpcRemoteException=" + rpcRemoteException +
                '}';
    }
}
