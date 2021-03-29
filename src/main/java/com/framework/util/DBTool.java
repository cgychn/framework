package com.framework.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.framework.context.TransactionManager;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBTool {

    public static <T> T query (String sql, Class<T> t) throws SQLException {
        Connection connection = TransactionManager.getCurrentConnection(true);
        System.out.println(connection);
        JSONArray result = new JSONArray();
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
            result.add(jsonObject);
        }

        T o = JSON.toJavaObject(result, t);
        System.out.println(o);
        return o;
    }

    public static int update (String sql) throws SQLException {
        // 更新
        Connection connection = TransactionManager.getCurrentConnection(true);
        System.out.println(connection);
        Statement statement = connection.createStatement();
        return statement.executeUpdate(sql);
    }

}
