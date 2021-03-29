package com.framework.db;

import com.alibaba.fastjson.JSONObject;
import com.framework.transaction.TransactionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBTool {

    public static <T> List<T> query (String sql, Class<T> t) throws SQLException {
        Connection connection = TransactionManager.getCurrentConnection(true);
        System.out.println(connection);
        List<T> result = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        while (resultSet.next()) {
            System.out.println(t);
            JSONObject jsonObject = new JSONObject();
            int columnCount = resultSetMetaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                jsonObject.put(resultSetMetaData.getColumnLabel(i), resultSet.getObject(resultSetMetaData.getColumnLabel(i)));
            }
            result.add(JSONObject.toJavaObject(jsonObject, t));
        }
        return result;
    }

    public static <T> T queryOne (String sql, Class<T> t) throws SQLException {
        List<T> res = query(sql, t);
        return res.size() > 0 ? res.get(0) : null;
    }

    public static int update (String sql) throws SQLException {
        // 更新
        Connection connection = TransactionManager.getCurrentConnection(true);
        System.out.println(connection);
        Statement statement = connection.createStatement();
        return statement.executeUpdate(sql);
    }

}
