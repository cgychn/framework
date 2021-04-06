package com.framework.rpc.task;


import com.framework.test.User;
import com.framework.util.InputStreamUtil;

import java.io.*;
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
            // 先读8个字节，这8个字节表示数据包的长度
            long packLen = InputStreamUtil.getStreamLen(socketInputStream);
            System.out.println(packLen);
            InputStream newInputStream = InputStreamUtil.cloneInputStream(socketInputStream, packLen - 8);
            // 转换成对象流
            ObjectInputStream objectInputStream = new ObjectInputStream(newInputStream);
            User u = (User) objectInputStream.readObject();
            System.out.println(u);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
