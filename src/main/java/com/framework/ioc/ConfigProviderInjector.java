package com.framework.ioc;

import com.framework.annotation.framework.CfgProvider;
import com.framework.config.MyFrameworkCfgContext;
import com.framework.config.prop.reader.PropertyReader;
import com.framework.config.yml.reader.YmlReader;
import com.framework.context.MyFrameworkContext;

import java.util.Map;

public class ConfigProviderInjector implements Injector {

    private static ConfigProviderInjector configProviderInjector = new ConfigProviderInjector();

    private ConfigProviderInjector () {}

    public static ConfigProviderInjector getInstance() {
        return configProviderInjector;
    }

    @Override
    public void inject(Class cls) {
        if (cls.isAnnotationPresent(CfgProvider.class)) {
            try {
                // 看看这个类拿的是哪个配置文件
                CfgProvider cfgProvider = (CfgProvider) cls.getDeclaredAnnotation(CfgProvider.class);
                String cfgName = cfgProvider.cfgFileName();
                if (cfgName.equals("")) {
                    // 未指定配置文件，do nothing
                } else {
                    // 指定了配置文件，将配置文件中的配置加载到map中
                    Map<String, Object> res = null;
                    if (cfgName.endsWith(".prop")) {
                        res = PropertyReader.readCfgFileByCfgName(cfgName);
                    } else if (cfgName.endsWith(".yml")) {
                        res = YmlReader.readCfgFileByCfgName(cfgName);
                    }
                    // 加载到上下文
                    MyFrameworkCfgContext.setMany(res);
                }
                // 将当前类加载到
                MyFrameworkContext.set(cls, cls.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
