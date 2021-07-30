package com.framework.rpc.server;

import com.framework.context.MyFrameworkContext;
import com.framework.rpc.entity.RemoteResultBean;
import com.framework.rpc.exception.RPCRemoteException;
import com.framework.rpc.entity.hearbeat.HeartBeatPing;
import com.framework.rpc.entity.hearbeat.HeartBeatPong;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务端的消息处理器
 */
public class ServerMessageHandler {

    // socket连接
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    // 是否出现异常
    private AtomicBoolean isException = new AtomicBoolean(false);
    // 异常时回调
    private MessageSendErrorCallBack messageSendErrorCallBack;
    private AtomicBoolean lastHeartBeatGot = new AtomicBoolean(true);
    // 最后一次处理消息的时间（socket增加空闲超时机制）
    private long lastHandleMessageTime = System.currentTimeMillis();
    // socket异常是否已经被处理（socket异常只能被处理一次）
    private AtomicBoolean isExceptionHandled = new AtomicBoolean(false);

    /**
     * 绑定
     * @param socket
     * @param messageSendErrorCallBack
     */
    public void bindSocket (Socket socket, MessageSendErrorCallBack messageSendErrorCallBack) {
        this.socket = socket;
        this.messageSendErrorCallBack = messageSendErrorCallBack;
        // 单独开线程处理（一个serverSocketHandler会占用两个线程，一个负责发心跳，一个负责处理业务逻辑）
        MyFrameworkContext.getFrameWorkThreadPool().exeTask(() -> {
            try {
                while (true && !isException.get()) {
                    if (!lastHeartBeatGot.get()) { continue; }
                    // 收到心跳后继续 ping（10s后）
                    Thread.sleep(10000);
                    HeartBeatPing heartBeatPing = new HeartBeatPing();
                    heartBeatPing.setMsg("heartbeat request from client");
                    // 包装
                    RemoteResultBean remoteResultBeanForHeartbeatPing = new RemoteResultBean();
                    remoteResultBeanForHeartbeatPing.setHeartBeatPing(heartBeatPing);
                    sendMessage(remoteResultBeanForHeartbeatPing);
                    lastHeartBeatGot.set(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.messageSendErrorCallBack.toDo(e, this.isExceptionHandled);
                this.isException.set(true);
            }
        });
        startListenMessage();
    }

    /**
     * 开启监听，监听输入流（从服务器读取数据）
     */
    private void startListenMessage() {
        try {
            // 主动先发送一个心跳给客户端
            HeartBeatPing heartBeatPing = new HeartBeatPing();
            heartBeatPing.setMsg("heartbeat request from server");
            // 包装ping
            RemoteResultBean remoteResultBeanForHeartbeatPing = new RemoteResultBean();
            remoteResultBeanForHeartbeatPing.setHeartBeatPing(heartBeatPing);
            sendMessage(remoteResultBeanForHeartbeatPing);

            objectInputStream = getSocketObjectInputStream();
            // 必须读取到所有的
            String implClassName;
            String methodName;
            Class[] paramTypes;
            Object[] args;
            // 不断读取服务器的数据
            while (true && !isException.get()) {
                System.out.println(lastHandleMessageTime + " || " + (System.currentTimeMillis() - 30000));
                if (lastHandleMessageTime < System.currentTimeMillis() - 30000) {
                    // 如果这个信道空闲了30s，就关闭socket（超时机制）
                    socket.close();
                    return;
                }
                Object object = objectInputStream.readObject();
                System.out.println("obj : " + object);
                if (!(object instanceof RemoteResultBean)) {
                    // 不处理非包装类
                    continue;
                }
                System.out.println("读取到obj：" + object);
                RemoteResultBean remoteResultBean = (RemoteResultBean) object;
                // 判断是否是心跳包
                if (remoteResultBean.getHeartBeatPong() != null) {
                    handleHeartBeat(remoteResultBean);
                } else {
                    // 处理正常业务逻辑
                    implClassName = (String) remoteResultBean.getObjs()[0];
                    methodName = (String) remoteResultBean.getObjs()[1];
                    paramTypes = (Class[]) remoteResultBean.getObjs()[2];
                    args = (Object[]) remoteResultBean.getObjs()[3];
                    // 直到接受到全部的参数，才处理消息
                    handleMessage(implClassName, methodName, paramTypes, args);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.messageSendErrorCallBack.toDo(e, this.isExceptionHandled);
            this.isException.set(true);
        }
    }

    /**
     * 处理心跳
     * @param remoteResultBean
     */
    public void handleHeartBeat(RemoteResultBean remoteResultBean) {
        // 单独开一个线程回复，防止阻塞
        HeartBeatPong heartBeatPong = remoteResultBean.getHeartBeatPong();
        lastHeartBeatGot.set(true);
        System.out.println("客户端的心跳回应：" + heartBeatPong.getMsg());
    }

    // 服务器目前不需要主动发非心跳消息给客户端，先注掉
//    /**
//     * 发消息，并处理回复
//     * @param sender
//     * @param callBack
//     */
//    public void sendMessage (MsgSender sender, OnMessageCallBack callBack) {
//        try {
//            while (callBackHandled.get() == false) {
//                Thread.sleep(100);
//            }
//            // 设置回调
//            if (callBack != null) {
//                this.callBack = callBack;
//                this.callBackHandled.set(false);
//            }
//            OutputStream outputStream = this.socket.getOutputStream();
//            sender.sendContent(outputStream);
//        } catch (Exception e) {
//            e.printStackTrace();
//            this.messageSendErrorCallBack.toDo(e);
//            this.isException.set(true);
//        }
//    }

    // 发消息统一在这里发
    public synchronized void sendMessage (RemoteResultBean remoteResultBean) {
        try {
            System.out.println("发送请求：" + remoteResultBean);
            objectOutputStream = getSocketObjectOutputStream();
            objectOutputStream.writeObject(remoteResultBean);
            objectOutputStream.flush();
        } catch (Exception e) {
            // 发送消息报错关闭socket
            e.printStackTrace();
            this.messageSendErrorCallBack.toDo(e, this.isExceptionHandled);
            this.isException.set(true);
        }
//        objectOutputStream.reset();
    }

    /**
     * 处理消息
     * @param implClassName
     * @param methodName
     * @param paramTypes
     * @param args
     */
    public void handleMessage(String implClassName, String methodName, Class[] paramTypes, Object[] args) {
        try {
            // 更新处理消息时间
            lastHandleMessageTime = System.currentTimeMillis();
            // 读写流的循序：类名 -> 方法名 -> 参数类型 -> 参数值
            Class destCls = Class.forName(implClassName);
            Object bean = MyFrameworkContext.getJustByClass(destCls);
            Method method = destCls.getMethod(methodName, paramTypes);
            // invoke方法后回写给客户端
            Object[] msg = {method.invoke(bean, args)};
            RemoteResultBean remoteResultBean = new RemoteResultBean();
            remoteResultBean.setObjs(msg);
            System.out.println("sendMessage：" + remoteResultBean);
            sendMessage(remoteResultBean);
        } catch (Exception e) {
            e.printStackTrace();
            // 报错发异常信息给client
            RPCRemoteException rpcRemoteException = new RPCRemoteException(e.getMessage());
            rpcRemoteException.setServerException(true);
            // 包装异常类
            RemoteResultBean remoteResultBean = new RemoteResultBean();
            remoteResultBean.setRpcRemoteException(rpcRemoteException);
            sendMessage(remoteResultBean);
        }
    }


    private synchronized ObjectOutputStream getSocketObjectOutputStream () throws IOException {
        if (this.objectOutputStream == null) {
            this.objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
        }
        return this.objectOutputStream;
    }

    private synchronized ObjectInputStream getSocketObjectInputStream () throws IOException {
        if (this.objectInputStream == null) {
            this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
        }
        return this.objectInputStream;
    }


    /**
     * 消息或者心跳发送失败的回调
     */
    public interface MessageSendErrorCallBack {
        /**
         * 回调方法，返回异常
         */
        void toDo (Exception e, AtomicBoolean exceptionHandled);
    }
}
