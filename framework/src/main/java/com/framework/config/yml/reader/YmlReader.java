package com.framework.config.yml.reader;

import com.framework.config.selector.ConfigFileSelector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class YmlReader {

    // 先禁用，等后面找到合适的yml解析器
    public static Map<String, Object> readCfgFileByCfgName(String cfgName) throws IOException {
        // 先用选择器自动过滤优先级，并得到优先级最高的配置文件
        return null;
    }

}
