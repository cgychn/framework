package com.framework.config.selector;

import com.framework.context.MyFrameworkContext;
import com.framework.main.Main;
import com.framework.util.PathUtil;

import java.io.File;

/**
 * 配置文件选择器
 * 优先级：（1）jar包所在目录的上一级下的etc目录下的配置文件，如：
 *        /myservice:
 *          -- /bin/myservice.jar
 *          -- /etc/application.prop
 *       （2）jar包同级目录下的配置文件，如：
 *        /myservice:
 *          -- /bin/myservice.jar
 *          -- /bin/application.prop
 *       （3）jar包/项目内 resource 目录中的 application.prop
 * 配置文件类型
 */
public class ConfigFileSelector {

    /**
     * 加载指定名称的配置文，并按照指定的优先级加载，如果文件不存在，就直接返回null
     * @param cfgName
     * @return
     */
    public static File getSelectedConfigFile (String cfgName) {
        File destFile = null;
//        System.out.println(PathUtil.getJarPackGrandRootEtcPath(MyFrameworkContext.getMainClass()) + cfgName);
//        System.out.println(PathUtil.getJarPackRootPath(MyFrameworkContext.getMainClass()) + cfgName);
//        System.out.println(PathUtil.getProjectResourcePath(MyFrameworkContext.getMainClass()) + cfgName);
        // 按优先级获取配置文件
        destFile = new File(PathUtil.getJarPackGrandRootEtcPath(MyFrameworkContext.getMainClass()) + cfgName);
        if (destFile.exists()) {
            System.out.println(destFile.getAbsolutePath() + "存在");
            return destFile;
        }
        destFile = new File(PathUtil.getJarPackRootPath(MyFrameworkContext.getMainClass()) + cfgName);
        if (destFile.exists()) {
            System.out.println(destFile.getAbsolutePath() + "存在");
            return destFile;
        }
        destFile = new File(PathUtil.getProjectResourcePath(MyFrameworkContext.getMainClass()) + cfgName);
        if (destFile.exists()) {
            System.out.println(destFile.getAbsolutePath() + "存在");
            return destFile;
        }
        return null;
    }
}
