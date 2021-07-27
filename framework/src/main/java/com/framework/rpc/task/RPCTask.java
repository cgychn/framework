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
        this.serverMessageHandler.bindSocket(socket, (err) -> {
            System.out.println(err);
            onlineSocketCount.getAndAdd(-1);
        });
    }
}
