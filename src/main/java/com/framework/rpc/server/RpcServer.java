package com.framework.rpc.server;

import com.framework.config.MyFrameworkCfgContext;
import com.framework.rpc.task.RPCTask;
import com.framework.util.ThreadPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RpcServer {
    ServerSocket serverSocket;
    // 引入线程池
    ThreadPool threadPool = ThreadPool.getThreadPoolInstance(30);

    public RpcServer () throws IOException {
        serverSocket = new ServerSocket(MyFrameworkCfgContext.get("framework.myrpc.provide.servicePort", Integer.class));

        while (true) {
            Socket socket = serverSocket.accept();
            // 开线程处理socket
            threadPool.exeTask(new RPCTask(socket));
        }
    }

    public static void startRPCServer () throws IOException {
        // 如果当前的服务也需要注册到注册中心，判断条件是服务是否配置了provider/只订阅，
        if (MyFrameworkCfgContext.getSubNodes("framework.myrpc.provide").size() == 0
                || MyFrameworkCfgContext.get("framework.myrpc.provide.justSubscribe", Boolean.class) == true) {
            return;
        }
        RpcServer rpcServer = new RpcServer();
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
