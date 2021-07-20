package com.framework.transaction;

import com.framework.util.DBPool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TransactionManager {

    // 连接池
//    private static DBPool dbPool = DBPool.getInstance(
//            20,
//            "root",
//            "123456",
//            "ams?serverTimezone=GMT%2B8&useSSL=false",
//            3306,
//            "localhost"
//    );
    private static DBPool dbPool = DBPool.getInstance();

    static ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    public static void createATransaction () throws SQLException {
        Connection connection = getCurrentConnection(true);
        connection.setAutoCommit(false);
    }

    public static Connection getCurrentConnection (boolean forceCreate) {
        Connection connection = connectionThreadLocal.get();
        // 如果强制创建 && connection没有被创建才会创建
        if (connection == null && forceCreate) {
            try {
//                Class.forName("com.mysql.jdbc.Driver");
                // 读配置文件
                Connection newConnection = dbPool.getConnFromPool();
                connectionThreadLocal.set(newConnection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return connectionThreadLocal.get();
    }

    public static void closeConnection () {
        Connection connection = getCurrentConnection(false);
        try {
            dbPool.restoreConnToPool(connection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 弱引用，手动释放
            connectionThreadLocal.remove();
        }
    }

    public static void rollback() throws SQLException {
        getCurrentConnection(false).rollback();
    }

    public static void commit() throws SQLException {
        getCurrentConnection(false).commit();
    }
}
