package com.framework.rpc.register.registry;

import com.framework.rpc.register.entiy.RegisterClassEntity;
import com.framework.rpc.register.entiy.RegisterMethodEntity;
import org.apache.zookeeper.*;

import java.io.IOException;


/**
 * 使用zookeeper作为注册中心，当服务启动后，当前服务先将当前服务注册到zookeeper中，再从zookeeper中订阅所需要的服务
 * 数据结构：
 * myrpc:
 * |__[servicename,...]:
 *   |__providers:
 *     |__[ip:port,...]:（一个服务可能有多个提供者（集群），实现负载均衡）
 *       |__[interface1,...]:
 *         |__[implement1,...]
 */
public class ZookeeperRegistry implements Watcher, Registry {

    // 这些将来都会抽离到配置文件中

    public static String serviceName = "testService";

    public static String serviceIp = "10.0.0.23";

    public static String port = "9098";

    public static String zkIPs = "192.168.85.30:2181";

    public static int timeout = 20000;

    private static ZooKeeper zooKeeper;

    private Object waiter = new Object();

    private static ZookeeperRegistry zookeeperRegistry = new ZookeeperRegistry();

    private ZookeeperRegistry () {
        this.connect();
    }

    public static ZookeeperRegistry getInstance() {
        return zookeeperRegistry;
    }

    public void connect () {
        try {
            synchronized (waiter) {
                if(zooKeeper == null){
                    // ZK客户端允许我们将ZK服务器的所有地址都配置在这里
                    zooKeeper = new ZooKeeper(zkIPs, timeout, this);
                }
                waiter.notifyAll();
            }
        } catch (IOException e) {
            System.out.println("连接创建失败，发生 InterruptedException , e " + e.getMessage() + e);
        }

    }

    public void close(){
        try {
            synchronized (waiter) {
                if(zooKeeper != null){
                    zooKeeper.close();
                }
                waiter.notifyAll();
            }
        } catch (InterruptedException e) {
            System.out.println("release connection error ," + e.getMessage() + e);
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.println("收到的消息：" + watchedEvent);
        if (watchedEvent.getType() != Event.EventType.None) {
            return;
        }
        switch(watchedEvent.getState()) {
            case SyncConnected:
                //zk连接建立成功,或者重连成功
                waiter.notifyAll();
                System.out.println("Connected...");
                break;
            case Expired:
                // session过期,这是个非常严重的问题,有可能client端出现了问题,也有可能zk环境故障
                // 此处仅仅是重新实例化zk client
                System.out.println("Expired(重连)...");
                connect();
                break;
            case Disconnected:
                System.out.println("链接断开，或session迁移....");
                break;
            case AuthFailed:
                close();
                throw new RuntimeException("ZK Connection auth failed...");
            default:
                break;
        }

    }

    @Override
    public void doRegister(RegisterClassEntity registerClassEntity) throws KeeperException, InterruptedException {
        this.connect();
        // 将该服务内所有的接口、接口实现、方法都注册到zookeeper中
        for (RegisterMethodEntity method : registerClassEntity.getMethodEntities()) {
            String path = "/myrpc/" +
                    serviceName +
                    "/providers/" +
                    serviceIp + ":" + port +
                    "/" + registerClassEntity.getInterfaceName() +
                    "/" + registerClassEntity.getCurrentClassName() +
                    "/" + method.getMethodName() + "||" + String.join(",", method.getMethodArgs()) + "||" + method.getReturnType();
            System.out.println(path);
            createNodeRecursively(path);
        }
    }

    private void createNodeRecursively (String path) {
        String[] pathParts = path.split("/");
        for (String p : pathParts) {
            System.out.print(p + ",");
        }
        createNode("/" + pathParts[0] , pathParts, 0);
    }

    private void createNode (String father, String[] parts, int index) {
        try {
            father = father.endsWith("/") ? father.substring(0, father.length() - 1) : father;
            System.out.println("currentPath : " + father + "/" + parts[index]);
            zooKeeper.create(
                    father + "/" + parts[index],
                    "".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT
            );
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (index == parts.length - 1) {
                return;
            }
            createNode(father + "/" + parts[index], parts, index + 1);
        }
    }

}
