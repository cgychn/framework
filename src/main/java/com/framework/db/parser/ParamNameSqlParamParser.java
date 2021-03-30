package com.framework.db.parser;

import com.framework.annotation.Param;

import java.lang.reflect.Parameter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamNameSqlParamParser implements SqlParamParser {

    // 解析 #{ paramName } 类型的传参，先把参数填入，然后有运算再运算
    @Override
    public String generateSql(String rawSql, Parameter[] parameters, Object[] args) {

        for (int i = 0; i < parameters.length; i ++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(Param.class)) {
                Param param = parameter.getDeclaredAnnotation(Param.class);
                String paramName = param.value();

                String regex = "\\#\\{([\\s\\S]*" + paramName + "[^}]*[\\s\\S]*)\\}";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(rawSql);
                while (matcher.find()) {
                    System.out.println(matcher.group());
                }

            }
        }

        return null;
    }

    public static void main(String[] args) {
        String[] parameters = {"as", "bbb", "ccc", "ddd"};
        for (int i = 0; i < parameters.length; i ++) {

            String paramName = parameters[i];
            String regex = "\\#\\{((" + paramName + ")[^}]?)\\}";
            System.out.println(regex);
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher("select * from my where id = #{ as }, #{as}, #{cs}");
            while (matcher.find()) {
                System.out.println(matcher.group());
            }

        }
    }

}
