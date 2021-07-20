package com.framework.db.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 框架默认带的二级缓存，使用hashmap缓存
 */
public class LocalCache implements Cache {

    /**
     * 缓存
     */
    private static Map<String, HashMap<String, CacheBean>> cacheMap = new HashMap<>();

    @Override
    public void cacheSqlResult(String nameSpace, String sql, Object value) {
        cacheSqlResult(nameSpace, sql, value, -1L);
    }

    @Override
    public void cacheSqlResult(String nameSPace, String sql, Object value, Long timeOut) {
        boolean hasNameSpace = false;
        synchronized (cacheMap) {
            if (cacheMap.get(nameSPace) != null) {
                hasNameSpace = true;
            }
        }
        if (hasNameSpace) {
            Map<String, CacheBean> nameSpaceCache = cacheMap.get(nameSPace);
            CacheBean cacheBean = new CacheBean();
            cacheBean.setTimeOut(timeOut);
            cacheBean.setTimeOutTimeStamp(timeOut == -1 ? null : (new Date().getTime() + timeOut));
            cacheBean.setNameSpace(nameSPace);
            cacheBean.setSql(sql);
            cacheBean.setValue(value);
            nameSpaceCache.put(sql, cacheBean);
        }
    }

    @Override
    public void clearCache(String nameSpace) {
        cacheMap.remove(nameSpace);
    }

    @Override
    public Object getCacheValue(String nameSpace, String sql) {
        Map<String, CacheBean> nameSpaceCache = cacheMap.get(nameSpace);
        if (nameSpaceCache != null) {
            CacheBean bean = nameSpaceCache.get(sql);
            if ((bean != null && new Date().getTime() < bean.getTimeOutTimeStamp()) || bean.getTimeOut() == -1) {
                // 命中缓存
                return bean.getValue();
            }
            // 超时
            if (new Date().getTime() >= bean.getTimeOutTimeStamp()) {
                nameSpaceCache.remove(sql);
            }
        }
        return null;
    }


}
