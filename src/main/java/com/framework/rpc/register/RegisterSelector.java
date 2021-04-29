package com.framework.rpc.register;

import com.framework.config.MyFrameworkCfgContext;
import com.framework.rpc.register.entiy.RegistryConfigItem;
import com.framework.rpc.register.entiy.RemoteClassEntity;
import com.framework.rpc.register.zookeeper.ZookeeperRegister;
import com.framework.rpc.register.zookeeper.ZookeeperRegistry;
import com.framework.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 注册中心选择器，该选择器会根据配置文件选择注册中心，
 * 希望在本地缓存一份注册表，这样可以避免服务调用时频繁访问注册中心，造成不必要的压力，
 * 当本地配置不可用时，再向注册中心申请新的注册表
 */
public class RegisterSelector {

    private static List<RegistryConfigItem> enabledReg = new ArrayList<>();

    static {
        findRegistryInCfg();
    }



    public static void registerServiceToEnabledRegistry(Class cls) {
        System.out.println("into reg");
        // 寻找已经启用的注册中心（在 myrpc.registry 下的子节点（一层关系））
//        findRegistryInCfg();

        Boolean justSub = MyFrameworkCfgContext.get("framework.myrpc.provide.justSubscribe", Boolean.class);
        // 遍历每个注册中心
        for (RegistryConfigItem registryConfigItem : enabledReg) {
            if (justSub != null) {
                // 服务提供者全局配置，对所有注册中心生效
                registryConfigItem.setJustSubscribe(justSub);
            }
            switch (registryConfigItem.getType()) {
                case "zookeeper":
                    System.out.println("使用zookeeper注册中心");
                    // zookeeper注册中心，
                    ZookeeperRegister.getInstance().registerToRegistry(cls, registryConfigItem);
                    break;
                default:
                    System.out.println("未支持的注册中心");
            }
        }


        // 其他注册中心
        // ...
    }

    /**
     * 在配置文件中寻找注册中心
     */
    private static void findRegistryInCfg () {
        // 这里拿的是所有注册中心的类型
        List<String> regs = MyFrameworkCfgContext.getSubNodes("framework.myrpc.registry")
                .stream()
                .collect(Collectors.toList());
        if (regs.size() == 0) {
            // 没有注册中心
            System.out.println("未找到已配置的注册中心");
        } else {
            // 找到注册中心
            // 遍历类型（zookeeper, ...）
            for (String regTypeName : regs) {
                // 在注册中心类型下找子注册项
                List<String> subs = MyFrameworkCfgContext.getSubNodes("framework.myrpc.registry." + regTypeName)
                        .stream()
                        .collect(Collectors.toList());
                // 这里注册中心的配置如果加了，需要修改
                if (subs.contains("ips") || subs.contains("timeout")) {
                    // 说明配置没有要使用多个该类型的注册中心
                    RegistryConfigItem registryConfigItem = new RegistryConfigItem();
                    registryConfigItem.setType(regTypeName);
                    registryConfigItem.setIps(
                            MyFrameworkCfgContext.get("framework.myrpc.registry." + regTypeName + ".ips", String.class)
                    );
                    registryConfigItem.setTimeout(
                            MyFrameworkCfgContext.get("framework.myrpc.registry." + regTypeName + ".timeout", Integer.class)
                    );
                    // 添加注册中心的实例到注册表
                    switch (regTypeName) {
                        case "zookeeper":
                            // 使用zookeeper注册中心
                            registryConfigItem.setRegistry(new ZookeeperRegistry(registryConfigItem));
                            break;
                        default:
                            break;
                    }

                    // 加到列表中
                    enabledReg.add(registryConfigItem);
                } else {
                    // 配置项中有多个注册中心
                    for (String subRegNameInCurrentRegType : subs) {
                        RegistryConfigItem registryConfigItem = new RegistryConfigItem();
                        registryConfigItem.setType(regTypeName);
                        registryConfigItem.setName(subRegNameInCurrentRegType);
                        registryConfigItem.setIps(
                                MyFrameworkCfgContext.get("framework.myrpc.registry." + regTypeName + "." + subRegNameInCurrentRegType + ".ips", String.class)
                        );
                        registryConfigItem.setTimeout(
                                MyFrameworkCfgContext.get("framework.myrpc.registry." + regTypeName + "." + subRegNameInCurrentRegType + ".timeout", Integer.class)
                        );
                        switch (regTypeName) {
                            case "zookeeper":
                                // 使用zookeeper注册中心
                                registryConfigItem.setRegistry(new ZookeeperRegistry(registryConfigItem));
                                break;
                            default:
                                break;
                        }

                        // 加到列表中
                        enabledReg.add(registryConfigItem);
                    }
                }
            }
        }
    }

    /**
     * 从注册中心寻找服务
     * @param serviceName
     * @param provider
     * @param interfaceName
     * @param destRemoteImplClassName
     * @return
     */
    public static RemoteClassEntity findRemoteClassInEnabledRegistry (
            String serviceName,
            String provider,
            String interfaceName,
            String destRemoteImplClassName,
            String registryName
    ) {
        System.out.println("registryName: " + registryName);
        RemoteClassEntity remoteClassEntity = null;
        try {
            // 没有配置多个注册中心
            if (registryName == null) {
                RegistryConfigItem registryConfigItem = enabledReg.get(0);
                switch (registryConfigItem.getType()) {
                    case "zookeeper":
                        remoteClassEntity = registryConfigItem.getRegistry().findRemoteClass(
                                serviceName,
                                provider,
                                interfaceName,
                                destRemoteImplClassName
                        );
                        return remoteClassEntity;
                    default:
                        return null;
                }
            } else {
                // 配置了多个注册中心
                for (RegistryConfigItem registryConfigItem : enabledReg) {
                    if (registryConfigItem.getName().equals(registryName)) {
                        switch (registryConfigItem.getType()) {
                            case "zookeeper":
                                remoteClassEntity = registryConfigItem.getRegistry().findRemoteClass(
                                        serviceName,
                                        provider,
                                        interfaceName,
                                        destRemoteImplClassName
                                );
                                return remoteClassEntity;
                            default:
                                return null;
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
