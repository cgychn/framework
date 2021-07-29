package com.framework.db;

import com.alibaba.fastjson.JSONObject;
import com.framework.transaction.TransactionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DBTool {

    //======================================================直接使用statement=====================================================

    /**
     * 查询
     * @param sql
     * @param t
     * @param <T>
     * @return
     * @throws SQLException
     */
    public static <T> Collection<T> query (String sql, Class<T> t) throws SQLException {
        Connection connection = TransactionManager.getCurrentConnection(true);
        System.out.println(connection);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        return forEachRes(resultSet, t);
    }

    /**
     * 查询单个值
     * @param sql
     * @param t
     * @param <T>
     * @return
     * @throws SQLException
     */
    public static <T> T queryOne (String sql, Class<T> t) throws SQLException {
        Collection<T> res = query(sql, t);
        if (res.size() > 1) {
            throw new SQLException("多条结果");
        }
        return res.stream().collect(Collectors.toList()).get(0);
    }

    /**
     * 更新
     * @param sql
     * @return
     * @throws SQLException
     */
    public static int update (String sql) throws SQLException {
        // 更新
        Connection connection = TransactionManager.getCurrentConnection(true);
        System.out.println(connection);
        Statement statement = connection.createStatement();
        return statement.executeUpdate(sql);
    }



    //=====================================================使用prepareStatement=====================================================




    /**
     * 使用prepareStatement更新
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public static int updateUsePrepareStatement (String sql, Object[] args, String[] jdbcTypes) throws SQLException {
        // 更新
        PreparedStatement statement = loadPrepareStatement(sql, args, jdbcTypes);
        return statement.executeUpdate();
    }

    /**
     * 使用prepareStatement查询
     * @param sql
     * @param args
     * @param jdbcTypes
     * @param t
     * @param <T>
     * @return
     * @throws SQLException
     */
    public static <T> Collection<T> queryUsePrepareStatement (String sql, Object[] args, String[] jdbcTypes, Class<T> t) throws SQLException {
        if (args.length != jdbcTypes.length) throw new SQLException("参数类型个数和值个数不对应！");

        PreparedStatement statement = loadPrepareStatement(sql, args, jdbcTypes);
        ResultSet resultSet = statement.executeQuery();
        return forEachRes(resultSet, t);
    }

    /**
     * 使用prepareStatement查询单个值
     * @param sql
     * @param args
     * @param jdbcTypes
     * @param t
     * @param <T>
     * @return
     * @throws SQLException
     */
    public static <T> T queryOneUsePrepareStatement (String sql, Object[] args, String[] jdbcTypes, Class<T> t) throws SQLException {
        Collection<T> res = queryUsePrepareStatement(sql, args, jdbcTypes, t);
        if (res.size() > 1) {
            throw new SQLException("多条结果");
        }
        return res.stream().collect(Collectors.toList()).get(0);
    }



    //======================================================封装工具内私有方法===========================================================



    /**
     * 遍历结果集，并取cast结果类型
     * @param resultSet
     * @param t
     * @param <T>
     * @return
     * @throws SQLException
     */
    private static <T> Collection<T> forEachRes (ResultSet resultSet, Class<T> t) throws SQLException {
        Collection<T> result = new LinkedList<>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            System.out.println(t);
            JSONObject jsonObject = new JSONObject();
            int columnCount = resultSetMetaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                jsonObject.put(resultSetMetaData.getColumnLabel(i), resultSet.getObject(resultSetMetaData.getColumnLabel(i)));
            }
            // 单独处理Integer，Long，Short类型
            if (t.equals(Integer.class) || t.equals(Long.class) || t.equals(Short.class)) {
                if (columnCount == 1 && (jsonObject.get(resultSetMetaData.getColumnLabel(0)) instanceof Number)) {
                    result.add(t.cast(jsonObject.get(resultSetMetaData.getColumnLabel(0))));
                }
            } else {
                result.add(JSONObject.toJavaObject(jsonObject, t));
            }

        }
        return result;
    }

    /**
     * 获取PreparedStatement，并装配参数
     * @param sql
     * @param args
     * @param jdbcTypes
     * @return
     * @throws SQLException
     */
    private static PreparedStatement loadPrepareStatement (String sql, Object[] args, String[] jdbcTypes) throws SQLException {
        Connection connection = TransactionManager.getCurrentConnection(true);
        PreparedStatement statement = connection.prepareStatement(sql);
        System.out.println(args[0].toString());
        System.out.println("sql : " + sql);
        for (int i = 0; i < args.length ; i++) {
            System.out.println(args[i] + " , " + jdbcTypes[i]);
            if (jdbcTypes[i] != null && !jdbcTypes[i].equals("")) {
                statement.setObject(
                        i + 1,
                        args[i],
                        JDBCType.valueOf(jdbcTypes[i])
                );
            } else {
                statement.setObject(
                        i + 1,
                        args[i]
                );
            }
        }
        return statement;
    }

}
