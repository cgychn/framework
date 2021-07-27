package com.framework.rpc.task;


import com.framework.context.MyFrameworkContext;
import com.framework.rpc.server.ServerMessageHandler;
import com.framework.util.InputStreamUtil;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RPCTask implements Runnable {

    private Socket socket;
    private AtomicInteger onlineSocketCount;
    private ServerMessageHandler serverMessageHandler;

    public RPCTask(Socket socket, AtomicInteger onlineSocketCount) {
        this.socket = socket;
        this.serverMessageHandler = new ServerMessageHandler();
        this.onlineSocketCount = onlineSocketCount;
    }

    @Override
    public void run() {
        // 给socket绑定处理器
        this.serverMessageHandler.bindSocket(socket, (err) -> {
            System.out.println(err);
            onlineSocketCount.getAndAdd(-1);
        });
    }
}
