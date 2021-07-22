package com.framework.db.proxy;

import com.framework.annotation.db.EnableCache;
import com.framework.annotation.db.Modifying;
import com.framework.annotation.db.Query;
import com.framework.db.DBTool;
import com.framework.db.cache.Cache;
import com.framework.db.cache.CacheBean;
import com.framework.db.parser.ParamNameSqlParamParser;
import com.framework.db.parser.SqlParamParserResult;
import com.framework.util.StringUtil;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class MapperProxy implements InvocationHandler {

    private Map<Class, Cache> currentCacheImplMap = new HashMap<>();

    public Object bind (Class cls) {
        return Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 是 insert/update
        Query query = method.getDeclaredAnnotation(Query.class);
        String sql = query.sql();
        // 用args 匹配sql中的占位符
        Parameter[] parameters = method.getParameters();
        // 这里换成prepareStatement，需要将注解上的sql解析为prepareStatement类型
        SqlParamParserResult sqlParamParserResult = new ParamNameSqlParamParser()
                .generateSql(sql, parameters, args);
        sql = sqlParamParserResult.getDesSql();

        // 调试sql解析后的结果
        System.out.println(sql);

        // 拿到这个方法的缓存信息
        CacheDetail cacheDetail = getMapperCacheDetail(method);

        // 修改操作
        if (method.isAnnotationPresent(Modifying.class)) {
            int rows = DBTool.updateUsePrepareStatement(
                    sql,
                    sqlParamParserResult.getSqlArgs(),
                    sqlParamParserResult.getJdbcTypes()
            );

            // 清空命名空间的缓存
            if (cacheDetail != null) {
                // 所有的cache实现类必须实现cache接口
                Cache mapperCache = getCacheInstance(cacheDetail.getCacheImplClass());
                // 调用清空
                mapperCache.clearCache(cacheDetail.getNameSpace());
            }

            return rows;
        } else {
            // 查询操作
            Type t = null;
            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
                for (Type type : actualTypeArguments) {
                    System.out.println("type " + type);
                    t = type;
                }
            }

            // 进入sql查询之前，先从缓存拿
            // 返回列表或者单个结果
            if (returnType instanceof Collection) {
                Collection collection;

                String cacheSqlHash = "";
                if (cacheDetail != null) {
                    // 启用了cache，生成cachehash todo ?
                    cacheSqlHash = query.sql()
                            + " || "
                            + method.getReturnType().getTypeName()
                            + " || "
                            + String.join(",", Arrays.stream(parameters).map(x -> {return x.getType().getTypeName();}).collect(Collectors.toList())) + " || "
                            + String.join(",", Arrays.stream(args).map(x -> {return x.toString();}).collect(Collectors.toList()));
                    // 从cache中拿
                    Object val = getCacheInstance(cacheDetail.getCacheImplClass())
                            .getCacheValue(cacheDetail.getNameSpace(), cacheSqlHash);
                    if (val != null) {
                        // 直接拿缓存中的
                        return val;
                    }
                }

                // 缓存中没有数据，到数据库中拿，并塞入缓存
                if (t != null) {
                    collection = DBTool.queryUsePrepareStatement(
                            sql,
                            sqlParamParserResult.getSqlArgs(),
                            sqlParamParserResult.getJdbcTypes(),
                            Class.forName(t.getTypeName())
                    );
                } else {
                    collection = DBTool.queryUsePrepareStatement(
                            sql,
                            sqlParamParserResult.getSqlArgs(),
                            sqlParamParserResult.getJdbcTypes(),
                            HashMap.class
                    );
                }

                if (returnType instanceof List) {
                    collection = (Set) collection.stream().collect(Collectors.toSet());
                } else if (returnType instanceof Set) {
                    collection = (List) collection.stream().collect(Collectors.toList());
                }

                Class returnTypeClass = Class.forName(returnType.getTypeName());
                if (cacheDetail != null) {
                    // 缓存到缓存中
                    getCacheInstance(cacheDetail.getCacheImplClass()).cacheSqlResult(
                            cacheDetail.getNameSpace(),
                            cacheSqlHash,
                            returnTypeClass.cast(collection),
                            cacheDetail.getTimeout()
                    );
                }

                return returnTypeClass.cast(collection);
            } else {
                if (returnType.equals(Void.TYPE)) {
                    throw new Exception("查询类的sql返回值不能为空");
                }
                return DBTool.queryOneUsePrepareStatement(
                        sql,
                        sqlParamParserResult.getSqlArgs(),
                        sqlParamParserResult.getJdbcTypes(),
                        Class.forName(returnType.getTypeName())
                );
            }
        }
    }

    /**
     * 获取mapper的缓存配置详情
     * @param method
     * @return
     */
    private CacheDetail getMapperCacheDetail (Method method) {
        try {
            // 如果没有被注解说明没有启用缓存
            if (!method.getDeclaringClass().isAnnotationPresent(EnableCache.class)
                    && !method.isAnnotationPresent(EnableCache.class)) {
                return null;
            }
            String nameSpace = null;
            Class cacheImplClass = null;
            long timeout = -1;
            if (method.getDeclaringClass().isAnnotationPresent(EnableCache.class)) {
                // mapper（类）上面加了 @EnableCache
                EnableCache enableCache = method.getDeclaringClass().getDeclaredAnnotation(EnableCache.class);
                // 未定义命名空间
                if (StringUtil.isEmpty(enableCache.nameSpace())) {
                    nameSpace = method.getDeclaringClass().getName();
                } else {
                    nameSpace = enableCache.nameSpace();
                }
                // 获取cache的实现类
                cacheImplClass = Class.forName(enableCache.cacheImplClass());
                timeout = enableCache.timeOut();
            }
            if (method.isAnnotationPresent(EnableCache.class)) {
                // mapper中的方法上加了 @EnableCache，覆盖类上面的 @EnableCache 配置
                EnableCache enableCache = method.getDeclaredAnnotation(EnableCache.class);
                if (!StringUtil.isEmpty(enableCache.nameSpace())) {
                    nameSpace = enableCache.nameSpace();
                }
                cacheImplClass = Class.forName(enableCache.cacheImplClass());
                timeout = enableCache.timeOut();
            }
            return new CacheDetail.Builder()
                    .nameSpace(nameSpace)
                    .cacheImplClass(cacheImplClass)
                    .timeout(timeout)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取cache接口实现类的单例
     * @param cls
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private Cache getCacheInstance (Class cls) throws IllegalAccessException, InstantiationException {
        Cache cacheImpl = currentCacheImplMap.get(cls);
        if (currentCacheImplMap.get(cls) != null) {
            return cacheImpl;
        }
        currentCacheImplMap.put(cls, (Cache) cls.newInstance());
        return currentCacheImplMap.get(cls);
    }

    /**
     * mapper 的缓存配置详情entity，使用构建者模型
     */
    private static class CacheDetail {

        private String nameSpace;
        private Class cacheImplClass;
        private long timeout;

        @Override
        public String toString() {
            return "CacheDetail{" +
                    "nameSpace='" + nameSpace + '\'' +
                    ", cacheImplClass=" + cacheImplClass +
                    ", timeout=" + timeout +
                    '}';
        }

        public String getNameSpace() {
            return nameSpace;
        }

        public Class getCacheImplClass() {
            return cacheImplClass;
        }

        public long getTimeout() {
            return timeout;
        }

        CacheDetail (Builder builder) {
            this.nameSpace = builder.nameSpace;
            this.cacheImplClass = builder.cacheImplClass;
            this.timeout = builder.timeout;
        }

        public static class Builder {
            private String nameSpace;
            private Class cacheImplClass;
            private long timeout;

            public Builder nameSpace(String nameSpace) {
                this.nameSpace = nameSpace;
                return this;
            }

            public Builder cacheImplClass (Class cacheImplClass) {
                this.cacheImplClass = cacheImplClass;
                return this;
            }

            public Builder timeout (long timeout) {
                this.timeout = timeout;
                return this;
            }

            public CacheDetail build () {
                return new CacheDetail(this);
            }
        }
    }


    public static void main(String[] args) {
        Collection collection = new LinkedList<>();

        collection.add(new CacheDetail.Builder().nameSpace("").build());
        collection.add(new CacheDetail.Builder().nameSpace("").build());
        collection.add(new CacheDetail.Builder().nameSpace("").build());
        collection.add(new CacheDetail.Builder().nameSpace("").build());
        collection.add(new CacheDetail.Builder().nameSpace("").build());

        Set set = (Set) collection.stream().collect(Collectors.toSet());
        List list = (ArrayList) collection.stream().collect(Collectors.toList());

        set.stream().forEach(x -> {
            System.out.println(x.toString());
        });
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).toString());
        }
        System.out.println(list.getClass().getTypeName());
    }


}
