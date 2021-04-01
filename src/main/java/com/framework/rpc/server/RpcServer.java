package com.framework.rpc.server;

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
        serverSocket = new ServerSocket(9098);

        while (true) {
            Socket socket = serverSocket.accept();
            // 开线程处理socket
            threadPool.exeTask(new RPCTask(socket));
        }
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
