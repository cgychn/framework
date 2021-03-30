package com.framework.db.parser;

import com.framework.annotation.Param;
import com.framework.util.StringUtil;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamNameSqlParamParser implements SqlParamParser {

    // 解析 #{ paramName } 类型的传参，先把参数填入，然后有运算再运算
    // 由于该sql语句最后会被转换成jdbc prepareStatement的sql语句（参数为？，所以需要对参数重新排序）
    @Override
    public SqlParamParserResult generateSql(String rawSql, Parameter[] rawParameters, Object[] rawArgs) {

        String regex = "\\#\\{([^}]*)\\}";
        // 到rawSql中匹配
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(rawSql);
        List<Object> destArgs = new ArrayList<>();
        List<String> destJDBCTypeStrings = new ArrayList<>();
        while (matcher.find()) {
            System.out.println(matcher.group());
            String paramStringInSql = matcher.group();
            // 如果有两个部分[0]变量名[1]变量类型，如果仅有一个部分[0]变量名
            String[] parts = paramStringInSql.replace("#{", "").replace("}", "").trim().split(",");
            String paramNameString = parts[0].trim();
            String paramJDBCType = null;
            if (parts.length == 2) {
                paramJDBCType = parts[1]
                        .trim()
                        .replaceAll(" ", "")
                        .replaceAll("jdbcType=", "");
                // 根据 paramJDBCType 转换 paramString
            }
            // 到参数列表中匹配
            for (int i = 0; i < rawParameters.length; i++) {
                if (rawParameters[i].isAnnotationPresent(Param.class)) {
                    Param param = rawParameters[i].getDeclaredAnnotation(Param.class);
                    String paramName = param.value();
                    // 匹配到参数名
                    if (paramNameString.equals(paramName)) {
                        // 将这个参数对应的对象（值）放入到list中
                        destArgs.add(rawArgs[i]);
                        destJDBCTypeStrings.add(paramJDBCType);
                        // 后将这个参数的位置替换为？
                        System.out.println(rawSql + ",,,," + paramStringInSql);
                        rawSql = StringUtil.replaceFirstWithOutReg(rawSql, paramStringInSql, "?");
                        System.out.println(rawSql);
                        break;
                    }
                }
            }
        }
        SqlParamParserResult sqlParamParserResult = new SqlParamParserResult();
        sqlParamParserResult.setDesSql(rawSql);
        sqlParamParserResult.setJdbcTypes(new String[destJDBCTypeStrings.size()]);
        sqlParamParserResult.setSqlArgs(new Object[destArgs.size()]);
//        System.out.println(rawSql);

        return sqlParamParserResult;
    }


}
