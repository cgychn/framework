package com.framework.rpc.register.zookeeper;

import com.framework.config.MyFrameworkCfgContext;
import com.framework.rpc.register.entiy.RegisterClassEntity;
import com.framework.rpc.register.entiy.RegisterMethodEntity;
import com.framework.rpc.register.entiy.RemoteClassEntity;
import com.framework.rpc.register.Registry;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.Random;


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

    // 每个服务提供者都需要配置这个
    // 这些将来都会抽离到配置文件中（在配置文件中必须以这种格式：myrpc.provide.serviceName  myrpc.provide.serviceIp  myrpc.provide.servicePort）
    public static String serviceName = (String) MyFrameworkCfgContext.get("framework.myrpc.provide.serviceName");
    public static String serviceIp = (String) MyFrameworkCfgContext.get("framework.myrpc.provide.serviceIp");
    public static String port = (String) MyFrameworkCfgContext.get("framework.myrpc.provide.servicePort");
    // 这两个属性控制当前服务 只订阅（只消费服务提供者提供的服务）/只注册（只在注册中心注册自己，而不消费别人提供的服务）
    // 只注册，不订阅（myrpc.provide.justRegister  myrpc.provide.justSubscribe）
    public static Boolean justRegister = MyFrameworkCfgContext.get("framework.myrpc.provide.justRegister", Boolean.class);
    // 只订阅，不注册
    public static Boolean justSubscribe = MyFrameworkCfgContext.get("framework.myrpc.provide.justSubscribe", Boolean.class);

    // 如果使用zookeeper注册中心需要配置，默认使用这个注册中心（使用其他的注册中心请无配置myrpc.registry.zookeeper.ips myrpc.registry.zookeeper.timeout）
    public static String zkIPs = (String) MyFrameworkCfgContext.get("framework.myrpc.registry.zookeeper.ips");
    public static int timeout = MyFrameworkCfgContext.get("framework.myrpc.registry.zookeeper.timeout", Integer.class);
    private static ZooKeeper zooKeeper;



    private Object waiter = new Object();

    private String serviceRootPath = "/myrpc";

    private static ZookeeperRegistry zookeeperRegistry = new ZookeeperRegistry();

    private ZookeeperRegistry () {
        this.connect();
        if (serviceName == null || serviceIp == null || port == null) {
            return;
        }
        // 注册之前先递归删除其和其子节点
        this.deleteRecursively(serviceRootPath + "/" + serviceName + "/providers/" + serviceIp + ":" + port);
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

    // 全新注册
    @Override
    public void doRegister(RegisterClassEntity registerClassEntity) {
//        this.connect();
        // 将该服务内所有的接口、接口实现、方法都注册到zookeeper中
        for (RegisterMethodEntity method : registerClassEntity.getMethodEntities()) {
            String path = serviceRootPath + "/" +
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

    /**
     * 寻找服务
     * @param serviceName 服务名称
     * @param provider 服务提供者
     * @param interfaceName 服务暴露的接口
     * @param implClassName 服务所暴露接口的实现类
     * @return
     */
    @Override
    public RemoteClassEntity findRemoteClass(String serviceName, String provider, String interfaceName, String implClassName) throws Exception {
        // 找服务
        RemoteClassEntity remoteClassEntity = new RemoteClassEntity();
        String holePath = "";
        // 用服务名寻找服务
        holePath += serviceRootPath + "/" + serviceName;
        Stat stat = zooKeeper.exists(holePath, false);
        if (stat == null) {
            // 这个服务不存在
            throw new Exception("服务在注册中心未注册");
        }
        // 选择该服务的提供者
        holePath += "/providers";
        if (provider == null || provider.equals("")) {
            // 未指定服务提供者，先随机数实现负载均衡
            List<String> children = zooKeeper.getChildren(holePath, false);
            int providerIndex = new Random().nextInt(children.size());
            provider = children.get(providerIndex);
        }
        // 找接口的实现
        holePath += "/" + provider + "/" + interfaceName;
        remoteClassEntity.setProvider(provider);
        remoteClassEntity.setInterfaceName(interfaceName);
        if (implClassName == null || implClassName.equals("")) {
            // 为指定接口的实现（仅针对改该接口只有一个实现起作用）
            List<String> children = zooKeeper.getChildren(holePath, false);
            // 该远程接口不止一个实现
            if (children.size() > 1) {
                throw new Exception("远程接口有多个实现，请指定远程接口的实现");
            } else {
                implClassName = children.get(0);
                holePath += "/" + implClassName;
                Stat statImp = zooKeeper.exists(holePath, false);
                if (statImp == null) {
                    throw new Exception("该接口的该实现不存在");
                }
            }
        } else {
            holePath += "/" + implClassName;
            Stat statImp = zooKeeper.exists(holePath, false);
            if (statImp == null) {
                throw new Exception("该接口的该实现不存在");
            }
        }
        remoteClassEntity.setImplClassName(implClassName);
        // 找接口中的方法
        List<String> children = zooKeeper.getChildren(holePath, false);
        for (String method : children) {
            RegisterMethodEntity registerMethodEntity = new RegisterMethodEntity();
            String[] parts = method.split("\\|\\|");
            String methodName = parts[0];
            String methodArgsString = parts[1];
            String methodReturnType = parts[2];
            String[] methodArgs = methodArgsString.split(",");
            registerMethodEntity.setMethodName(methodName);
            registerMethodEntity.setReturnType(methodReturnType);
            registerMethodEntity.setMethodArgs(methodArgs);
            remoteClassEntity.getMethodEntityList().add(registerMethodEntity);
        }
        System.out.println(remoteClassEntity);
        return remoteClassEntity;
    }

    @Override
    public RemoteClassEntity findRemoteClass(String serviceName, String interfaceName, String implClassName) throws Exception {
        return findRemoteClass(serviceName, null, interfaceName, implClassName);
    }

    @Override
    public RemoteClassEntity findRemoteClass(String serviceName, String interfaceName) throws Exception {
        return findRemoteClass(serviceName, null, interfaceName, null);
    }

    private void createNodeRecursively (String path) {
        String[] pathParts = path.split("/");
//        for (String p : pathParts) {
//            System.out.print(p + ",");
//        }
        createNode("/" + pathParts[0] , pathParts, 0);
    }

    private void createNode (String father, String[] parts, int index) {
        try {
            father = father.endsWith("/") ? father.substring(0, father.length() - 1) : father;
//            System.out.println("currentPath : " + father + "/" + parts[index]);
            if (zooKeeper.exists(father + "/" + parts[index], false) == null) {
                zooKeeper.create(
                        father + "/" + parts[index],
                        "".getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (index == parts.length - 1) {
                return;
            }
            createNode(father + "/" + parts[index], parts, index + 1);
        }
    }

    /**
     * 删除这个节点，并且删除这个节点下的所有子节点
     * @param path
     */
    private void deleteRecursively (String path) {

        try {
            List<String> children = zooKeeper.getChildren(path, false);
            for (String child : children) {
                System.out.println(child);
                deleteRecursively(path + "/" + child);
            }
//            System.out.println("delete: " + path);
            zooKeeper.delete(path, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
