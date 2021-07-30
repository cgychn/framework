package com.framework.rpc.register.zookeeper;

import com.framework.config.MyFrameworkCfgContext;
import com.framework.rpc.register.entiy.RegisterClassEntity;
import com.framework.rpc.register.entiy.RegisterMethodEntity;
import com.framework.rpc.register.entiy.RegistryConfigItem;
import com.framework.rpc.register.entiy.RemoteClassEntity;
import com.framework.rpc.register.Registry;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.*;


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

    // 如果使用zookeeper注册中心需要配置，默认使用这个注册中心（使用其他的注册中心请无配置myrpc.registry.zookeeper.ips myrpc.registry.zookeeper.timeout）
//    public String zkIPs = (String) MyFrameworkCfgContext.get("framework.myrpc.registry.zookeeper.ips");
//    public int timeout = MyFrameworkCfgContext.get("framework.myrpc.registry.zookeeper.timeout", Integer.class);
    public String zkIPs = "";
    public int timeout = 0;
    private ZooKeeper zooKeeper;
    // zookeeper节点的缓存
    private RegistryCacheNode cacheNode = new RegistryCacheNode();



    private Object waiter = new Object();

    private String serviceRootPath = "/myrpc";

    public ZookeeperRegistry (RegistryConfigItem registryConfigItem) {
        // 初始化缓存
        cacheNode.setNodeName("myrpc");
        cacheNode.setParent(null);
        cacheNode.setNodeZookeeperPath("/myrpc");
        cacheNode.setSubNodes(new HashMap<>());
        // 基本信息
        this.zkIPs = registryConfigItem.getIps();
        this.timeout = registryConfigItem.getTimeout();
        this.connect();
        loadAllServicesRegistryToCache();
        if (serviceName == null || serviceIp == null || port == null) {
            return;
        }
        // 注册之前先递归删除其和其子节点
        this.deleteRecursively(serviceRootPath + "/" + serviceName + "/providers/" + serviceIp + ":" + port);
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
        // 先从缓存中找服务，找不到在到zookeeper中找
        RemoteClassEntity cache = new RemoteClassEntity();
        cache = queryInCache(serviceName, provider, interfaceName, implClassName);
        System.out.println("cache : " + cache);
        if (cache != null) { return cache; }

        // 找服务
        RemoteClassEntity remoteClassEntity = new RemoteClassEntity();
        String holePath = "";
        // 用服务名寻找服务
        holePath += serviceRootPath + "/" + serviceName;
        Stat stat = zooKeeper.exists(holePath, true);
        if (stat == null) {
            // 这个服务不存在
            throw new Exception("该服务在注册中心未注册");
        }
        // 选择该服务的提供者
        holePath += "/providers";
        if (provider == null || provider.equals("")) {
            // 未指定服务提供者，随机数实现负载均衡
            List<String> children = zooKeeper.getChildren(holePath, true);
            int providerIndex = new Random().nextInt(children.size());
            provider = children.get(providerIndex);
        }
        // 找接口的实现
        holePath += "/" + provider + "/" + interfaceName;
        remoteClassEntity.setProvider(provider);
        remoteClassEntity.setInterfaceName(interfaceName);
        if (implClassName == null || implClassName.equals("")) {
            // 为指定接口的实现（仅针对改该接口只有一个实现起作用）
            List<String> children = zooKeeper.getChildren(holePath, true);
            // 该远程接口不止一个实现
            if (children.size() > 1) {
                throw new Exception("远程接口有多个实现，请指定远程接口的实现");
            } else {
                implClassName = children.get(0);
                holePath += "/" + implClassName;
                Stat statImp = zooKeeper.exists(holePath, true);
                if (statImp == null) {
                    throw new Exception("该接口的该实现不存在");
                }
            }
        } else {
            holePath += "/" + implClassName;
            Stat statImp = zooKeeper.exists(holePath, true);
            if (statImp == null) {
                throw new Exception("该接口的该实现不存在");
            }
        }
        remoteClassEntity.setImplClassName(implClassName);
        // 找接口中的方法
        List<String> children = zooKeeper.getChildren(holePath, true);
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
            if (zooKeeper.exists(father + "/" + parts[index], true) == null) {
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
            List<String> children = zooKeeper.getChildren(path, true);
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

    /**
     * 拿到所有在zookeeper注册中心注册的服务
     */
    public void loadAllServicesRegistryToCache () {
        try {
            System.out.println("--------------------------------加载配置以供缓存--------------------------------------");
            // 第一层（服务名）
            List<String> serviceNames = zooKeeper.getChildren(serviceRootPath, true);
            for (String serviceName : serviceNames) {
                RegistryCacheNode serviceCache = new RegistryCacheNode();
                serviceCache.setNodeName(serviceName);
                serviceCache.setNodeZookeeperPath(cacheNode.getNodeZookeeperPath() + "/" + serviceName);
                serviceCache.setSubNodes(new HashMap<>());
                serviceCache.setParent(cacheNode);
                cacheNode.getSubNodes().put(serviceName, serviceCache);
                // 第二层（提供者）
                List<String> providerNames = zooKeeper.getChildren(
                        "/" + cacheNode.getNodeName() + "/" + serviceName + "/providers",
                        true
                );
                for (String providerName : providerNames) {
                    RegistryCacheNode providerCache = new RegistryCacheNode();
                    providerCache.setNodeName(providerName);
                    providerCache.setNodeZookeeperPath(serviceCache.getNodeZookeeperPath() + "/providers/" + providerName);
                    providerCache.setSubNodes(new HashMap<>());
                    providerCache.setParent(serviceCache);
                    serviceCache.getSubNodes().put(providerName, providerCache);
                    // 第三层（接口名称）
                    List<String> interfaceNames = zooKeeper.getChildren(
                            "/" + cacheNode.getNodeName() + "/" + serviceName + "/providers/" + providerName,
                            true
                    );
                    for (String interfaceName : interfaceNames) {
                        RegistryCacheNode interfaceCache = new RegistryCacheNode();
                        interfaceCache.setNodeName(interfaceName);
                        interfaceCache.setNodeZookeeperPath(providerCache.getNodeZookeeperPath() + "/" + interfaceName);
                        interfaceCache.setSubNodes(new HashMap<>());
                        interfaceCache.setParent(providerCache);
                        providerCache.getSubNodes().put(interfaceName, interfaceCache);
                        // 第四层（接口实现类）
                        List<String> implClassNames = zooKeeper.getChildren(
                                "/" + cacheNode.getNodeName() + "/" + serviceName + "/providers/" + providerName + "/" + interfaceName,
                                true
                        );
                        for (String implClassName : implClassNames) {
                            RegistryCacheNode implClassCache = new RegistryCacheNode();
                            implClassCache.setNodeName(implClassName);
                            implClassCache.setNodeZookeeperPath(interfaceCache.getNodeZookeeperPath() + "/" + implClassName);
                            implClassCache.setSubNodes(new HashMap<>());
                            implClassCache.setParent(interfaceCache);
                            interfaceCache.getSubNodes().put(implClassName, implClassCache);
                            // 第五层（接口实现中的方法）
                            List<String> methodNames = zooKeeper.getChildren(
                                    "/" + cacheNode.getNodeName() + "/" + serviceName + "/providers/" + providerName + "/" + interfaceName + "/" + implClassName,
                                    true
                            );
                            for (String methodName : methodNames) {
                                RegistryCacheNode methodCache = new RegistryCacheNode();
                                methodCache.setNodeName(methodName);
                                methodCache.setNodeZookeeperPath(implClassCache.getNodeZookeeperPath() + "/" + methodName);
                                methodCache.setSubNodes(new HashMap<>());
                                methodCache.setParent(implClassCache);
                                implClassCache.getSubNodes().put(methodName, methodCache);
                            }
                        }
                    }
                }
            }
            System.out.println(cacheNode);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从缓存中查
     * @param serviceName
     * @param provider
     * @param interfaceName
     * @param implClassName
     */
    public RemoteClassEntity queryInCache (String serviceName, String provider, String interfaceName, String implClassName) {
        try {
            RemoteClassEntity remoteClassEntity = new RemoteClassEntity();
            HashMap<String, RegistryCacheNode> providerNodes = cacheNode.getSubNodes().get(serviceName).getSubNodes();
            System.out.println("providerNodes : " + providerNodes);
            if (provider == null || provider.equals("")) {
                Object[] keys = providerNodes.keySet().toArray();
                int providerIndex = new Random().nextInt(keys.length);
                provider = providerNodes
                        .get(keys[providerIndex].toString())
                        .getNodeName();
            }

            if (implClassName == null || implClassName.equals("")) {
                HashMap<String, RegistryCacheNode> implClassNodes = providerNodes
                        .get(provider)
                        .getSubNodes()
                        .get(interfaceName)
                        .getSubNodes();
                implClassName = implClassNodes.values().stream().findFirst().get().getNodeName();
            }

            HashMap<String, RegistryCacheNode> methods = providerNodes
                    .get(provider)
                    .getSubNodes()
                    .get(interfaceName)
                    .getSubNodes()
                    .get(implClassName).getSubNodes();

            remoteClassEntity.setProvider(provider);
            remoteClassEntity.setImplClassName(implClassName);
            remoteClassEntity.setInterfaceName(interfaceName);
            List<RegisterMethodEntity> registerMethodEntities = new ArrayList<>();
            for (RegistryCacheNode method : methods.values()) {
                RegisterMethodEntity registerMethodEntity = new RegisterMethodEntity();
                String[] parts = method.getNodeName().split("\\|\\|");
                String methodName = parts[0];
                String methodArgsString = parts[1];
                String methodReturnType = parts[2];
                String[] methodArgs = methodArgsString.split(",");
                registerMethodEntity.setMethodName(methodName);
                registerMethodEntity.setReturnType(methodReturnType);
                registerMethodEntity.setMethodArgs(methodArgs);
                registerMethodEntities.add(registerMethodEntity);
            }
            remoteClassEntity.setMethodEntityList(registerMethodEntities);
            return remoteClassEntity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // 缓存节点
    public static class RegistryCacheNode {
        RegistryCacheNode parent;
        String nodeName;
        String nodeZookeeperPath;
        HashMap<String, RegistryCacheNode> subNodes;

        public String getNodeZookeeperPath() {
            return nodeZookeeperPath;
        }

        public void setNodeZookeeperPath(String nodeZookeeperPath) {
            this.nodeZookeeperPath = nodeZookeeperPath;
        }

        public RegistryCacheNode getParent() {
            return parent;
        }

        public void setParent(RegistryCacheNode parent) {
            this.parent = parent;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public HashMap<String, RegistryCacheNode> getSubNodes() {
            return subNodes;
        }

        public void setSubNodes(HashMap<String, RegistryCacheNode> subNodes) {
            this.subNodes = subNodes;
        }

        @Override
        public String toString() {
            return "RegistryCacheNode{" +
                    "parent=" + parent +
                    ", nodeName='" + nodeName + '\'' +
                    ", nodeZookeeperPath='" + nodeZookeeperPath + '\'' +
                    '}';
        }
    }

}
