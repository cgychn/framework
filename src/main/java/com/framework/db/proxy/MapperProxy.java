package com.framework.db.proxy;

import com.framework.annotation.Modifying;
import com.framework.annotation.Query;
import com.framework.db.DBTool;
import com.framework.db.parser.ParamNameSqlParamParser;
import com.framework.db.parser.SqlParamParserResult;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MapperProxy implements InvocationHandler {

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
        // 构造一个 参数：值 的结构
//        for (int i = 0 ; i < parameters.length ; i++) {
//            sql = sql.replace(
//                    "#{" + parameters[i].getAnnotation(Param.class).value() + "}",
//                    "'" + args[i].toString() + "'"
//            );
//        }
        // 这里换成prepareStatement，需要将注解上的sql解析为prepareStatement类型
        SqlParamParserResult sqlParamParserResult = new ParamNameSqlParamParser()
                .generateSql(sql, parameters, args);
        sql = sqlParamParserResult.getDesSql();
        System.out.println(sql);
        if (method.isAnnotationPresent(Modifying.class)) {
            return DBTool.updateUsePrepareStatement(
                    sql,
                    sqlParamParserResult.getSqlArgs(),
                    sqlParamParserResult.getJdbcTypes()
            );
        } else {
            Type t = null;
            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
                for (Type type : actualTypeArguments) {
                    System.out.println("type " + type);
                    t = type;
                }
            }
            // 返回列表或者单个结果
            if (!(returnType instanceof List || returnType instanceof Set)) {
                if (t != null) {
                    return DBTool.queryUsePrepareStatement(
                            sql,
                            sqlParamParserResult.getSqlArgs(),
                            sqlParamParserResult.getJdbcTypes(),
                            Class.forName(t.getTypeName())
                    );
                } else {
                    return DBTool.queryUsePrepareStatement(
                            sql,
                            sqlParamParserResult.getSqlArgs(),
                            sqlParamParserResult.getJdbcTypes(),
                            HashMap.class
                    );
                }
            } else {
                return DBTool.queryOneUsePrepareStatement(
                        sql,
                        sqlParamParserResult.getSqlArgs(),
                        sqlParamParserResult.getJdbcTypes(),
                        Class.forName(returnType.getTypeName())
                );
            }
        }
    }
}
