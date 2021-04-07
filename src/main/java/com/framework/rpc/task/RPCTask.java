package com.framework.rpc.task;


import com.framework.context.MyFrameworkContext;
import com.framework.test.User;
import com.framework.util.InputStreamUtil;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class RPCTask implements Runnable {

    private Socket socket;

    public RPCTask (Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStream socketInputStream = null;
        try {
            socketInputStream = socket.getInputStream();
            // 先读8个字节，这8个字节表示数据包的长度（先去除长度校验，后期有必要再加）
//            long packLen = InputStreamUtil.getStreamLen(socketInputStream);
//            System.out.println(packLen);
//            InputStream newInputStream = InputStreamUtil.cloneInputStream(socketInputStream, packLen - 8);
            // 转换成对象流
            ObjectInputStream objectInputStream = new ObjectInputStream(socketInputStream);
            // 从客户端输入流中读取必要参数
            // 读写流的循序：类名 -> 方法名 -> 参数类型 -> 参数值
            String implClassName = (String) objectInputStream.readObject();
            String methodName = (String) objectInputStream.readObject();
            Class[] paramTypes = (Class[]) objectInputStream.readObject();
            Object[] args = (Object[]) objectInputStream.readObject();
            Class destCls = Class.forName(implClassName);
            Object bean = MyFrameworkContext.getJustByClass(destCls);
            Method method = destCls.getMethod(methodName, paramTypes);
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            // invoke方法后回写给客户端
            objectOutputStream.writeObject(method.invoke(bean, args));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
