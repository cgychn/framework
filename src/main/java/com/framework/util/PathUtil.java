package com.framework.util;

public class PathUtil {

    public static String getJarPackRootPath (Class cls)
    {
        System.out.println(cls);
        String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
        if(System.getProperty("os.name").contains("dows"))
        {
            path = path.substring(1);
        }
        if(path.contains("jar"))
        {
            path = path.substring(0,path.lastIndexOf("."));
            path = path.substring(0,path.lastIndexOf("/"));
            path = path.endsWith("/") ? path : path + "/";
            return path;
        }
        String res = path.replace("target/classes/", "");
        res = res.endsWith("/") ? res : res + "/";
        return res;
    }

    public static String getProjectResourcePath (Class cls) {
        String path = cls.getClassLoader().getResource("").getPath();
        System.out.println(path);
        return path;
    }

    public static String getJarPackGrandRootEtcPath (Class cls) {
        return getJarPackRootPath(cls) + "../etc/";
    }

}
