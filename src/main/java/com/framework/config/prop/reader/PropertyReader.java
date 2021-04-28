package com.framework.config.prop.reader;

import com.framework.config.selector.ConfigFileSelector;

import java.io.*;
import java.util.*;

public class PropertyReader {

    public static Map<String, Object> readCfgFileByCfgName(String cfgName) throws IOException {
        // 先用选择器自动过滤优先级，并得到优先级最高的配置文件
        File cfgFile = ConfigFileSelector.getSelectedConfigFile(cfgName);
        if (cfgFile == null) {
            return null;
        } else {
            // 读取配置文件中的配置，并存贮到map
            Map<String, Object> res = new HashMap<>();
            Properties properties = new Properties();
            properties.load(new FileInputStream(cfgFile));
            Set<String> propKeySet = properties.stringPropertyNames();
            for (String key : propKeySet) {
                if (res.get(key) != null) {
                    throw new IOException("配置文件 " + cfgName + " 中，配置项" + key + "，已存在");
                }
                res.put(key, properties.get(key));
            }
            return res;
        }
    }
}
