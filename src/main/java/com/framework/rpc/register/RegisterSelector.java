package com.framework.rpc.register;

import com.framework.config.MyFrameworkCfgContext;
import com.framework.rpc.register.zookeeper.ZookeeperRegister;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 注册中心选择器，该选择器会根据配置文件选择
 */
public class RegisterSelector {

    static String enabledReg = "";

    public static void registerServiceToEnabledRegistry(Class cls) {
        System.out.println("into reg");
        // 寻找已经启用的注册中心（在 myrpc.registry 下的子节点（一层关系））
        findRegistryInCfg();

        Boolean justSub = MyFrameworkCfgContext.get("framework.myrpc.provide.justSubscribe", Boolean.class);
        // 默认 justRegister 为false
        // 默认 justSub 为false
        if (null == justSub || false == justSub) {
            // 只有未配置只订阅或者只订阅为false才会向注册中心注册
            switch (enabledReg) {
                case "zookeeper":
                    System.out.println("使用zookeeper注册中心");
                    // zookeeper注册中心，
                    ZookeeperRegister.getInstance().registerToRegistry(cls);
                    break;
                default:
                    System.out.println("未使用注册中心，请指定");
                    return;
            }
        }


        // 其他注册中心
        // ...
    }

    /**
     * 寻找注册中心
     */
    private static void findRegistryInCfg () {
        List<String> regs = MyFrameworkCfgContext.getSubNodes("framework.myrpc.registry")
                .stream()
                .collect(Collectors.toList());
        if (regs.size() > 1) {
            System.out.println("该服务只能注册到一个注册中心");
        } else if (regs.size() == 0) {
            // 没有注册中心
            System.out.println("未找到已配置的注册中心");
        } else {
            enabledReg = regs.get(0);
        }
    }
}
