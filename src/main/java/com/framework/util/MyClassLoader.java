package com.framework.util;

import com.framework.annotation.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * 扫描包下路径 
 * 包括本地文件和jar包文件
 * @author ljb
 *
 */
public class MyClassLoader {

//    private Class<?> superStrategy = String.class;//接口类class 用于过滤 可以不要

    private static List<Class> eleStrategyList = new ArrayList<>();

    private static ClassLoader classLoader = MyClassLoader.class.getClassLoader();//默认使用的类加载器

    /**
     * 获取包下所有实现了superStrategy的类并加入list
     */
    public static List loadClass(String path){
        URL url = classLoader.getResource(path.replace(".", "/"));
        String protocol = url.getProtocol();
        if ("file".equals(protocol)) {
            // 本地自己可见的代码
            System.out.println("use local");
            findClassLocal(path);
        } else if ("jar".equals(protocol)) {
            // 引用jar包的代码
            System.out.println("use jar");
            findClassJar(path);
        }
        return eleStrategyList;
    }

    /**
     * 本地查找
     * @param packName
     */
    private static void findClassLocal(final String packName){
        URI url = null ;
        try {
            url = classLoader.getResource(packName.replace(".", "/")).toURI();
        } catch (URISyntaxException e1) {
            throw new RuntimeException("未找到策略资源");
        }

        File file = new File(url);
        file.listFiles(new FileFilter() {

            public boolean accept(File chiFile) {
                if(chiFile.isDirectory()){
                    findClassLocal(packName+"."+chiFile.getName());
                }
                if(chiFile.getName().endsWith(".class")){
                    Class<?> clazz = null;
                    try {
                        clazz = classLoader.loadClass(packName + "." + chiFile.getName().replace(".class", ""));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    System.out.println(chiFile);
                    eleStrategyList.add(clazz);
                    return true;
                }
                return false;
            }
        });

    }

    /**
     * jar包查找
     * @param packName
     */
    private static void findClassJar(final String packName){
        String pathName = packName.replace(".", "/");
        System.out.println(packName + ",," + pathName);
        JarFile jarFile  = null;
        try {
            URL url = classLoader.getResource(pathName);
            JarURLConnection jarURLConnection  = (JarURLConnection )url.openConnection();
            jarFile = jarURLConnection.getJarFile();
        } catch (IOException e) {
            throw new RuntimeException("未找到策略资源");
        }

        Enumeration<JarEntry> jarEntries = jarFile.entries();
        System.out.println(jarEntries);
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String jarEntryName = jarEntry.getName();
            if(jarEntryName.contains(pathName) && !jarEntryName.equals(pathName + "/")){
                //递归遍历子目录
//                if(jarEntry.isDirectory()){
//                    String clazzName = jarEntry.getName().replace("/", ".");
//                    int endIndex = clazzName.lastIndexOf(".");
//                    String prefix = null;
//                    if (endIndex > 0) {
//                        prefix = clazzName.substring(0, endIndex);
//                    }
//                    findClassJar(prefix);
//                }
                if(jarEntry.getName().endsWith(".class")){
                    Class<?> clazz = null;
                    try {
                        clazz = classLoader.loadClass(jarEntry.getName().replace("/", ".").replace(".class", ""));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    System.out.println("add : " + clazz);
                    eleStrategyList.add(clazz);
                }
            }

        }

    }

}
