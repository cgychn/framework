package com.framework.rpc.server;

import com.framework.config.MyFrameworkCfgContext;
import com.framework.context.MyFrameworkContext;
import com.framework.rpc.task.RPCTask;
import com.framework.util.ThreadPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcServer {
    ServerSocket serverSocket;
    // 引入线程池
    ThreadPool threadPool = MyFrameworkContext.getFrameWorkThreadPool();
    static Integer serverSocketMaxAcceptCount = MyFrameworkCfgContext.get("framework.serverSocket.maxConnectedSize", Integer.class) == null ? 50 : MyFrameworkCfgContext.get("framework.serverSocket.maxConnectedSize", Integer.class);

    public RpcServer () throws IOException {
        serverSocket = new ServerSocket(MyFrameworkCfgContext.get("framework.myrpc.provide.servicePort", Integer.class));
        AtomicInteger onlineSocketCount = new AtomicInteger(0);
        while (true) {
            System.out.println("max accept：" + serverSocketMaxAcceptCount.intValue() + ", 在线的socket数量：" + onlineSocketCount.get());
            if (serverSocketMaxAcceptCount.intValue() > onlineSocketCount.get()) {
                Socket socket = serverSocket.accept();
                // 开线程处理socket
                onlineSocketCount.getAndAdd(1);
                threadPool.exeTask(new RPCTask(socket, onlineSocketCount));
            } else {
                continue;
            }
        }
    }

    public static void startRPCServer () throws IOException {
        // 如果当前的服务也需要注册到注册中心，判断条件是服务是否配置了provider/只订阅，
        if (MyFrameworkCfgContext.getSubNodes("framework.myrpc.provide").size() == 0) {
            return;
        }
        new RpcServer();
    }

    // test
    public static void main(String[] args) {
        try {
            RpcServer rpcServer = new RpcServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
