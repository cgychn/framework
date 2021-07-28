package com.framework.rpc.client;

import com.framework.config.MyFrameworkCfgContext;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientSocketHandlerPool {

    static ClientSocketHandlerPool instance = null;

    /**
     * 连接池，ip:port 作为键，每个 ip:port 都有独立的连接池
     */
    static private ConcurrentHashMap<String, ConcurrentLinkedQueue<ClientMessageHandler>> pool = new ConcurrentHashMap<>();
    static private HashMap<String, AtomicInteger> socketPoolCountMap = new HashMap<>();
    static Integer socketPoolMaxCount = MyFrameworkCfgContext.get("framework.clientSocketPool.size", Integer.class) == null ?
            0 : MyFrameworkCfgContext.get("framework.clientSocketPool.size", Integer.class);

    public static synchronized ClientSocketHandlerPool getInstance() {
        if (instance == null) {
            instance = new ClientSocketHandlerPool();
        }
        return instance;
    }

    /**
     * 从连接池拿socket
     * @return
     */
    public ClientMessageHandler getSocketHandlerFromPool (String ip, int port) throws IOException, InterruptedException {
        String key = ip + ":" + port;

        // 连接池为空时初始化连接池
        synchronized (socketPoolCountMap) {
            if (socketPoolCountMap.get(key) == null) {
                socketPoolCountMap.put(key, new AtomicInteger(0));
            }
        }
        synchronized (pool) {
            if (pool.get(key) == null) {
                pool.put(key, new ConcurrentLinkedQueue<>());
            }
        }

        // 处理连接池中的连接
        synchronized (pool.get(key)) {
            while (true) {
                System.out.println(pool.get(key));
                // 链接池中无可用链接且连接池大小已达上限（等待或者直接返回）
                if (pool.get(key).size() == 0
                        && socketPoolCountMap.get(key).get() == socketPoolMaxCount) {
                    pool.get(key).wait();
                }

                // 连接池中有可用链接（直接取链接）
                if (pool.get(key).size() > 0) {
                    ClientMessageHandler h = pool.get(key).poll();
                    return h;
                }

                // 连接池中无可用链接，并且连接池大小未达上限（初始化链接放入连接池并取走）
                if (pool.get(key).size() == 0 && socketPoolCountMap.get(key).get() < socketPoolMaxCount) {
                    // 新建连接
                    Socket s = new Socket(ip, port);

                    // 给socket绑定处理器，并将handler加入到连接池中，并添加异常处理方法
                    ClientMessageHandler handler = new ClientMessageHandler();
                    handler.bindSocket(s, (err) -> {
                        System.out.println(err);
                        synchronized (pool.get(key)) {
                            pool.get(key).remove(handler);
                            socketPoolCountMap.get(key).getAndAdd(-1);
                        }
                    });

                    // 将刚添加的handler放入连接池
                    pool.get(key).offer(handler);
                    socketPoolCountMap.get(key).getAndAdd(1);

                    // 从连接池中取走连接
                    ClientMessageHandler h = pool.get(key).poll();
                    return h;
                }
            }
        }
    }

    /**
     * 把socket返还给连接池
     * @param clientMessageHandler
     */
    public void returnHandlerToPool (ClientMessageHandler clientMessageHandler, String ip, int port) {
        String key = ip + ":" + port;
        synchronized (pool.get(key)) {
            // 判断这个连接是否时异常连接，异常就不放入连接池
            if (!clientMessageHandler.getIsException().get()) {
                pool.get(key).offer(clientMessageHandler);
                pool.get(key).notifyAll();
            }
            System.out.println("返回了一个连接，现在的连接数：" + pool.get(key).size());
            System.out.println(pool.get(key));
        }
    }

}
