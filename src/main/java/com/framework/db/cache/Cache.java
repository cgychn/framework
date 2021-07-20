package com.framework.db.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数的二级缓存，缓存规则为：namespace -> sql，当namespace中有更新操作时，会清空namespace下所有的缓存，如果有分布式需求，自己实现
 */
public interface Cache {

    /**
     * 把sql的结果添加到缓存
     * @param nameSpace
     * @param sql
     * @param value
     */
    void cacheSqlResult (String nameSpace, String sql, Object value);

    /**
     * 把sql的结果添加到缓存（带超时机制）
     * @param nameSPace
     * @param sql
     * @param value
     * @param timeOut
     */
    void cacheSqlResult (String nameSPace, String sql, Object value, Long timeOut);

    /**
     * 清空缓存
     * @param nameSpace
     */
    void clearCache (String nameSpace);

    /**
     * 拿缓存的值
     * @param nameSpace
     * @param sql
     * @return
     */
    Object getCacheValue (String nameSpace, String sql);

}
