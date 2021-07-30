package com.framework.config;

import com.framework.config.prop.reader.PropertyReader;
import com.framework.config.yml.reader.YmlReader;

import java.io.IOException;
import java.util.*;

public class MyFrameworkCfgContext {

    private static Map<String, Object> cfgMap = new HashMap<>();

    // 加载主配置文件
    public static void loadMainProp (String mainPropName) throws IOException {
        Map<String, Object> res = null;
        Map<String, Object> resYml = YmlReader.readCfgFileByCfgName(mainPropName + ".yml");
        Map<String, Object> resProp = PropertyReader.readCfgFileByCfgName(mainPropName + ".prop");
        System.out.println(resYml + " " + resProp);
        if (resYml == null) {
            res = resProp;
        }
        if (resProp == null) {
            res = resYml;
        }
        if (res == null) {
            System.out.println("主配置文件未启用");
        } else {
            System.out.println("主配置文件启用");
            for (Map.Entry<String, Object> entry : res.entrySet()) {
                System.out.println(entry.getKey() + " " + entry.getValue());
                set(entry.getKey(), entry.getValue());
            }
        }
    }


    public static Object get (String key) {
        return cfgMap.get(key);
    }


    public static <T> T get (String key, Class<T> cls) {
        try {
            if (cls == Integer.class) {
                return (T) Integer.valueOf(get(key).toString());
            } else if (cls == Long.class) {
                return (T) Long.valueOf(get(key).toString());
            } else if (cls == Short.class) {
                return (T) Short.valueOf(get(key).toString());
            } else if (cls == Boolean.class) {
                return (T) Boolean.valueOf(get(key).toString());
            } else {
                return cls.cast(get(key));
            }
        } catch (Exception e) {
            System.out.println("配置为空");
            return null;
        }
    }


    public static void set (String key, Object obj) {
        if (cfgMap.get(key) == null) {
            cfgMap.put(key, obj);
        } else {
            // 配置项已存在
            System.out.println("配置项 " + key + " 已存在");
        }
    }

    public static void setMany (Map<String, Object> kVs) {
        Set<Map.Entry<String, Object>> entries = kVs.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            set(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 获取指定配置下所有子配置项，比如：a.b.c 下 a.b.c.d 和 a.b.c.d.g 和 a.b.c.e，会返回 d，e，但不会返回g
     * @param root
     * @return
     */
    public static Set<String> getSubNodes (String root) {
        Set<String> sets = new HashSet<>();
        for (String key : cfgMap.keySet()) {
//            System.out.println("key: " + key);
            if (key.startsWith(root)) {
                // 是这个配置的子项
                String sub = key.replace(root, "");
                if (sub.equals("")) {
                    // 就是本身
                } else {
                    String[] parts = sub.split("\\.");
                    sets.add(parts[1]);
                }
            }
        }
        return sets;
    }

}
