package com.framework.context;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TransactionManager {

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
                Class.forName("com.mysql.jdbc.Driver");
                // 读配置文件
                Connection newConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ams?serverTimezone=GMT%2B8&useSSL=false", "root", "123456");
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
            connection.close();
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
