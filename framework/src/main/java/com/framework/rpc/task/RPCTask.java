package com.framework.rpc.task;


import com.framework.rpc.server.ServerMessageHandler;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

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
        this.serverMessageHandler.bindSocket(socket, (err, exceptionHandled) -> {
            synchronized (exceptionHandled) {
                // 防止异常重复处理
                if (!exceptionHandled.get()) {
                    System.out.println(err);
                    onlineSocketCount.getAndAdd(-1);
                    System.out.println("异常处理方法处理了一个连接：" + onlineSocketCount.get());
                    exceptionHandled.set(true);
                }
            }
        });
    }
}
