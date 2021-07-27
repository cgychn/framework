package com.framework.rpc.server;

import com.framework.context.MyFrameworkContext;
import com.framework.rpc.hearbeat.HeartBeatPing;
import com.framework.rpc.hearbeat.HeartBeatPong;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务端的消息处理器
 */
public class ServerMessageHandler {

    // socket连接
    private Socket socket;
    // 是否出现异常
    private AtomicBoolean isException = new AtomicBoolean(false);
    // 回调是否已经被处理，如果没有被处理，sendMessage方法将被阻塞，直到被处理
//    private AtomicBoolean callBackHandled = new AtomicBoolean(true);
    // 异常时回调
    private MessageSendErrorCallBack messageSendErrorCallBack;

    /**
     * 绑定
     * @param socket
     * @param messageSendErrorCallBack
     */
    public void bindSocket (Socket socket, MessageSendErrorCallBack messageSendErrorCallBack) {
        this.socket = socket;
        this.messageSendErrorCallBack = messageSendErrorCallBack;
        startListenMessage();
    }

    /**
     * 开启监听，监听输入流（从服务器读取数据）
     */
    private void startListenMessage() {
        try {
            InputStream inputStream = this.socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

//            String implClassName = (String) objectInputStream.readObject();
//            String methodName = (String) objectInputStream.readObject();
//            Class[] paramTypes = (Class[]) objectInputStream.readObject();
//            Object[] args = (Object[]) objectInputStream.readObject();
            // 必须读取到所有的
            String implClassName = null;
            String methodName = null;
            Class[] paramTypes = null;
            Object[] args = null;
            // 不断读取服务器的数据
            while (true && !isException.get()) {
                System.out.println("进入循环");
                Object object = objectInputStream.readObject();
                System.out.println("读取到obj：" + object);
                // 判断是否是心跳包
                if (object instanceof HeartBeatPong) {
                    handleHeartBeat(object);
                } else {
                    // 处理正常业务逻辑
                    if (implClassName == null) {
                        System.out.println("set implClassName");
                        implClassName = (String) object;
                        continue;
                    }
                    if (methodName == null) {
                        System.out.println("set methodName");
                        methodName = (String) object;
                        continue;
                    }
                    if (paramTypes == null) {
                        System.out.println("set paramTypes");
                        paramTypes = (Class[]) object;
                        continue;
                    }
                    if (args == null) {
                        System.out.println("set args");
                        args = (Object[]) object;
                    }
                    // 直到接受到全部的参数，才处理消息
                    handleMessage(implClassName, methodName, paramTypes, args);
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
        HeartBeatPong heartBeatPong = (HeartBeatPong) obj;
        System.out.println("客户端的心跳回应：" + heartBeatPong.getMsg());
        // 收到心跳后继续 ping（10s后）
        try {
            Thread.sleep(10000);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            HeartBeatPing heartBeatPing = new HeartBeatPing();
            heartBeatPing.setMsg("heartbeat request from client");
            objectOutputStream.writeObject(heartBeatPing);
            objectOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            this.messageSendErrorCallBack.toDo(e);
            this.isException.set(true);
        }
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
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            // invoke方法后回写给客户端
            objectOutputStream.writeObject(method.invoke(bean, args));
        } catch (Exception e) {
            e.printStackTrace();
            this.messageSendErrorCallBack.toDo(e);
            this.isException.set(true);
        }
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
