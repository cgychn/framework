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
            // 先读64个字节，这8个字节表示数据包的长度
            byte[] lenBytes = new byte[8];
            // 一定会大于8个字节，应为前64个字节表示数据包的长度
            long packLen = 0l;
            // 阻塞直到读到64位
            while (socketInputStream.available() < 8) {}
            if (socketInputStream.read(lenBytes) != -1) {
                // 获取到数据包的长度
                ByteBuffer buffer = ByteBuffer.allocate(8);
                buffer.put(lenBytes, 0, 8);
                buffer.flip();
                packLen = buffer.getLong();
            }
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
