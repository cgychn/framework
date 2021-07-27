package com.framework.rpc.client;

import com.framework.rpc.hearbeat.HeartBeatPing;
import com.framework.rpc.hearbeat.HeartBeatPong;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 客户端的消息处理器（发送心跳和服务端通讯、）
 */
public class ClientMessageHandler {

    // 绑定的socket对象
    private Socket socket;
    // 绑定的回调
    private OnMessageCallBack callBack;
    // 回调是否已经被处理，如果没有被处理，sendMessage方法将被阻塞，直到被处理
    private AtomicBoolean callBackHandled = new AtomicBoolean(true);
    // 异常时回调
    private MessageSendErrorCallBack messageSendErrorCallBack;
    // 该连接是否异常
    private AtomicBoolean isException = new AtomicBoolean(false);

    /**
     * 给socket对象绑定处理器
     * @param socket
     */
    public void bindSocket (Socket socket, MessageSendErrorCallBack messageSendErrorCallBack) {
        this.socket = socket;
        this.messageSendErrorCallBack = messageSendErrorCallBack;
        this.startListenMessage();
    }

    /**
     * 开启监听，监听输入流（从服务器读取数据）
     */
    private void startListenMessage() {
        try {
            InputStream inputStream = this.socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            // 不断读取服务器的数据
            while (true) {
                Object object = objectInputStream.readObject();
                // 判断是否时心跳包
                if (object instanceof HeartBeatPing) {
                    handleHeartBeat(object);
                } else {
                    handleMessage(object);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发消息，并处理回复
     * @param sender
     * @param callBack
     */
    public void sendMessage (MsgSender sender, OnMessageCallBack callBack) {
        try {
            while (callBackHandled.get() == false) {
                Thread.sleep(10);
            }
            // 设置回调
            if (callBack != null) {
                this.callBack = callBack;
                this.callBackHandled.set(false);
            }
            OutputStream outputStream = this.socket.getOutputStream();
            sender.sendContent(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            this.messageSendErrorCallBack.toDo(e);
            this.isException.set(true);
        }
    }

    /**
     * 处理心跳
     * @param obj
     */
    public void handleHeartBeat(Object obj) {
        try {
            // 给服务器回应，代表自己在线
            HeartBeatPing heartBeatPing = (HeartBeatPing) obj;
            System.out.println("心跳包：" + heartBeatPing.getMsg());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            HeartBeatPong heartBeatPong = new HeartBeatPong();
            heartBeatPong.setMsg("heartbeat resp from client");
            objectOutputStream.writeObject(heartBeatPong);
        } catch (IOException e) {
            e.printStackTrace();
            this.messageSendErrorCallBack.toDo(e);
            this.isException.set(true);
        }
    }

    /**
     * 获得连接状态
     * @return
     */
    public AtomicBoolean getIsException() {
        return isException;
    }

    /**
     * 设置连接状态
     * @param isException
     */
    public void setIsException(AtomicBoolean isException) {
        this.isException = isException;
    }

    /**
     * 处理正常消息
     * @param obj
     */
    public void handleMessage(Object obj) {
        if (this.callBack != null) {
            this.callBack.toDo(obj);
            this.callBackHandled.set(true);
        }
    }

    /**
     * 回调接口
     */
    public interface OnMessageCallBack {
        /**
         * 服务器读取到的对象
         * @param obj
         */
        void toDo (Object obj);
    }

    /**
     * 发送消息接口，发送消息的逻辑写这里
     */
    public interface MsgSender {
        /**
         * 值提供输入流对象，在该方法中不允许有I和O的交互，只能写数据，读数据由handler统一处理
         * @param outputStream
         */
        void sendContent (OutputStream outputStream);
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
