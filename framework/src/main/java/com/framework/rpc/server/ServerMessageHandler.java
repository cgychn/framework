package com.framework.rpc.server;

import com.framework.context.MyFrameworkContext;
import com.framework.rpc.exception.RPCRemoteException;
import com.framework.rpc.hearbeat.HeartBeatPing;
import com.framework.rpc.hearbeat.HeartBeatPong;

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
    // 回调是否已经被处理，如果没有被处理，sendMessage方法将被阻塞，直到被处理
//    private AtomicBoolean callBackHandled = new AtomicBoolean(true);
    // 异常时回调
    private MessageSendErrorCallBack messageSendErrorCallBack;

    private AtomicBoolean lastHeartBeatGot = new AtomicBoolean(true);

    /**
     * 绑定
     * @param socket
     * @param messageSendErrorCallBack
     */
    public void bindSocket (Socket socket, MessageSendErrorCallBack messageSendErrorCallBack) {
        this.socket = socket;
        this.messageSendErrorCallBack = messageSendErrorCallBack;
        // 单独开线程处理
        MyFrameworkContext.getFrameWorkThreadPool().exeTask(() -> {
            try {
                while (true) {
                    if (!lastHeartBeatGot.get()) { continue; }
                    // 收到心跳后继续 ping（10s后）
                    Thread.sleep(10000);
                    HeartBeatPing heartBeatPing = new HeartBeatPing();
                    heartBeatPing.setMsg("heartbeat request from client");
                    Object[] msg = {heartBeatPing};
                    sendMessage(msg);
                    lastHeartBeatGot.set(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.messageSendErrorCallBack.toDo(e);
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
            objectOutputStream = getSocketObjectOutputStream();
            // 主动先发送一个心跳给客户端
            HeartBeatPing heartBeatPing = new HeartBeatPing();
            heartBeatPing.setMsg("heartbeat request from client");
            Object[] msg = {heartBeatPing};
            sendMessage(msg);

            objectInputStream = getSocketObjectInputStream();

            // 必须读取到所有的
            String implClassName = null;
            String methodName = null;
            Class[] paramTypes = null;
            Object[] args = null;
            // 不断读取服务器的数据
            while (true && !isException.get()) {
                Object object = objectInputStream.readObject();
                System.out.println("读取到obj：" + object);
                // 判断是否是心跳包
                if (object instanceof HeartBeatPong) {
                    handleHeartBeat(object);
                } else {
                    // 处理正常业务逻辑
                    if (implClassName == null) {
                        implClassName = (String) object;
                        continue;
                    }
                    if (methodName == null) {
                        methodName = (String) object;
                        continue;
                    }
                    if (paramTypes == null) {
                        paramTypes = (Class[]) object;
                        continue;
                    }
                    if (args == null) {
                        args = (Object[]) object;
                    }
                    // 直到接受到全部的参数，才处理消息
                    handleMessage(implClassName, methodName, paramTypes, args);
                    implClassName = null;
                    methodName = null;
                    paramTypes = null;
                    args = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理心跳
     * @param obj
     */
    public void handleHeartBeat(Object obj) {
        // 单独开一个线程回复，防止阻塞
        HeartBeatPong heartBeatPong = (HeartBeatPong) obj;
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
    public synchronized void sendMessage (Object[] msg) {
        try {
            objectOutputStream = getSocketObjectOutputStream();
            for (int i = 0; i < msg.length; i++) {
                objectOutputStream.writeObject(msg[i]);
            }
            objectOutputStream.flush();
        } catch (Exception e) {
            // 发送消息报错关闭socket
            e.printStackTrace();
            this.messageSendErrorCallBack.toDo(e);
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
            // 读写流的循序：类名 -> 方法名 -> 参数类型 -> 参数值
            Class destCls = Class.forName(implClassName);
            Object bean = MyFrameworkContext.getJustByClass(destCls);
            Method method = destCls.getMethod(methodName, paramTypes);
            objectOutputStream = getSocketObjectOutputStream();
            // invoke方法后回写给客户端
            Object[] msg = {method.invoke(bean, args)};
            sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
            // 报错发异常信息给client
            RPCRemoteException rpcRemoteException = new RPCRemoteException(e.getMessage());
            rpcRemoteException.setServerException(true);
            sendMessage(new Object[]{rpcRemoteException});
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
        void toDo (Exception e);
    }
}
