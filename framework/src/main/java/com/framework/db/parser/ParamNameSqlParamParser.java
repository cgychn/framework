package com.framework.db.parser;

import com.framework.annotation.db.Param;
import com.framework.util.StringUtil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 2021 - 7 -24 新改进，参数表达式和运算表达式在一个类中同时进行：
 * 通过正则表达式匹配到 #{} 和 ${} ,将 #{} 普通处理，${} 做算数运算处理
 */
public class ParamNameSqlParamParser implements SqlParamParser {

    private static ScriptEngineManager manager = new ScriptEngineManager();

    // 解析 #{ paramName } 类型的传参，先把参数填入，然后有运算再运算
    // 由于该sql语句最后会被转换成jdbc prepareStatement的sql语句（参数为？，所以需要对参数重新排序）
    @Override
    public SqlParamParserResult generateSql(String rawSql, Parameter[] rawParameters, Object[] rawArgs) {
        SqlParamParserResult sqlParamParserResult = new SqlParamParserResult();
        List<SqlParamBean> sqlParamBeanList = new ArrayList<>();
        // 处理取值表达式
        sqlParamBeanList.addAll(parseNormalExpression(rawSql, rawParameters, rawArgs));
        // 处理运算表达式
        sqlParamBeanList.addAll(parseArithmeticExpression(rawSql, rawParameters, rawArgs));
        // 根据index排序
        List<SqlParamBean> sqlParamBeanSortedList = sqlParamBeanList
                .stream()
                .sorted(Comparator.comparing(SqlParamBean::getCurrentSqlParamIndex))
                .collect(Collectors.toList());
        System.out.println("rawSql : " + rawSql);
        // 根据排好序的sqlParamBean处理sql
        List<Object> sqlArgs = new ArrayList<>();
        List<String> jdbcTypes = new ArrayList<>();
        for (SqlParamBean sqlParamBean : sqlParamBeanSortedList) {
            System.out.print(sqlParamBean + "\n");
            rawSql = StringUtil.replaceFirstWithOutReg(rawSql, sqlParamBean.getRawParam(), "?");
            sqlArgs.add(sqlParamBean.getValue());
            jdbcTypes.add(sqlParamBean.getJdbcType());
        }
        sqlParamParserResult.setDesSql(rawSql);
        sqlParamParserResult.setSqlArgs(sqlArgs.toArray(new Object[sqlArgs.size()]));
        sqlParamParserResult.setJdbcTypes(jdbcTypes.toArray(new String[sqlArgs.size()]));
        return sqlParamParserResult;
    }

    /**
     * 解析一般的表达式
     * @param rawSql
     * @param rawParameters
     * @param rawArgs
     * @return
     */
    private List<SqlParamBean> parseNormalExpression (String rawSql, Parameter[] rawParameters, Object[] rawArgs) {
        List<SqlParamBean> sqlParamBeans = new LinkedList<>();
        String regex = "\\#\\{([^}]*)\\}";
        // 到rawSql中匹配
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(rawSql);
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
            // 到参数列表里面找
            for (int i = 0; i < rawParameters.length; i++) {
                if (rawParameters[i].isAnnotationPresent(Param.class)) {
                    Param param = rawParameters[i].getDeclaredAnnotation(Param.class);
                    // 方法参数上注解的值
                    String paramName = param.value();
                    // 匹配到参数名
                    if (paramNameString.equals(paramName)) {
                        // 将这个参数对应的对象（值）放入到list中
                        // 记录下标
                        SqlParamBean sqlParamBean = new SqlParamBean();
                        sqlParamBean.setRawParam(paramStringInSql);
                        sqlParamBean.setJdbcType(paramJDBCType);
                        sqlParamBean.setValue(rawArgs[i]);
                        sqlParamBean.setCurrentSqlParamIndex(matcher.start());
                        sqlParamBeans.add(sqlParamBean);
                        break;
                    }
                }
            }
        }
        return sqlParamBeans;
    }

    /**
     * 解析算术表达式
     * @param rawSql
     * @param rawParameters
     * @param rawArgs
     * @return
     */
    private List<SqlParamBean> parseArithmeticExpression (String rawSql, Parameter[] rawParameters, Object[] rawArgs) {
        ScriptEngine scriptEngine = manager.getEngineByName("js");
        List<SqlParamBean> sqlParamBeans = new LinkedList<>();
        String regex = "\\$\\{([^}]*)\\}";
        // 到rawSql中匹配
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(rawSql);
        while (matcher.find()) {
            System.out.println(matcher.group());
            String paramStringInSql = matcher.group();
            // 如果有两个部分[0]变量名[1]变量类型，如果仅有一个部分[0]变量名
            String express = paramStringInSql.replace("${", "").replace("}", "").trim();
            // 解析出express的jdbcType
            String[] parts = express.split(",");
            // 检查最后一节
            String last = parts[parts.length - 1];
            String jdbcType = checkAndGetJDBCType(last);

            if (jdbcType != null) {
                // 如果存在jdbcType（parts的长度肯定 >= 2）
                if (parts.length > 2) {
                    express = String.join(
                            ",",
                            Arrays.stream(parts)
                                    .collect(Collectors.toList())
                                    .subList(0, parts.length - 1)
                    ).trim();
                } else {
                    express = parts[0].trim();
                }
            } else {
                if (parts.length > 1) {
                    express = String.join(",", parts);
                } else {
                    express = parts[0];
                }
            }

            Object expressValue = null;
            // 到参数列表里面找
            for (int i = 0; i < rawParameters.length; i++) {
                if (rawParameters[i].isAnnotationPresent(Param.class)) {
                    Param param = rawParameters[i].getDeclaredAnnotation(Param.class);
                    // 方法参数上注解的值
                    String paramName = param.value();
                    // 匹配到参数名
                    if (checkParamInExpress(paramName, express)) {
                        scriptEngine.put(paramName, rawArgs[i]);
                    }
                }
            }
            try {
                expressValue = scriptEngine.eval(express);
                SqlParamBean sqlParamBean = new SqlParamBean();
                sqlParamBean.setRawParam(paramStringInSql);
                sqlParamBean.setJdbcType(jdbcType);
                sqlParamBean.setValue(jdbcType == null ? ((Double) expressValue).longValue() : expressValue);
                sqlParamBean.setCurrentSqlParamIndex(matcher.start());
                sqlParamBeans.add(sqlParamBean);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
        return sqlParamBeans;
    }

    /**
     * 检查表达式中是否存在参数
     * @param paramName
     * @param express
     * @return
     */
    public boolean checkParamInExpress (String paramName, String express) {
        if (StringUtil.isEmpty(express) || StringUtil.isEmpty(paramName)) {
            return false;
        }
        if (express.contains(paramName)) {
            return true;
        }
        return false;
    }

    /**
     * 检查并返回jdbcType，检查不到就返回null
     * @param lastPart
     * @return
     */
    public String checkAndGetJDBCType (String lastPart) {
        if (StringUtil.isEmpty(lastPart)) {
            return null;
        }
        String jdbcType = null;
        if (!(lastPart = lastPart.replaceAll(" ", "")).startsWith("jdbcType=")) {
            return null;
        } else {
            // 是jdbcType
            jdbcType = lastPart.replace("jdbcType=", "");
            return jdbcType;
        }
    }

    /**
     * sql参数类
     */
    static class SqlParamBean {
        String rawParam;
        String jdbcType;
        Object value;
        int currentSqlParamIndex;

        public int getCurrentSqlParamIndex() {
            return currentSqlParamIndex;
        }

        public void setCurrentSqlParamIndex(int currentSqlParamIndex) {
            this.currentSqlParamIndex = currentSqlParamIndex;
        }

        public String getRawParam() {
            return rawParam;
        }

        public void setRawParam(String rawParam) {
            this.rawParam = rawParam;
        }

        public String getJdbcType() {
            return jdbcType;
        }

        public void setJdbcType(String jdbcType) {
            this.jdbcType = jdbcType;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "SqlParamBean{" +
                    "rawParam='" + rawParam + '\'' +
                    ", jdbcType='" + jdbcType + '\'' +
                    ", value=" + value +
                    ", currentSqlParamIndex=" + currentSqlParamIndex +
                    '}';
        }
    }

    public static void main(String[] args) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        new Thread(() -> {
            String str = "(a - b + b - b + c + c + a - a) + c + 'c'";
            ScriptEngine engine = manager.getEngineByName("js");
            System.out.println(engine);
            engine.put("a", 10);
            engine.put("b", 11);
            engine.put("c", 20);
            engine.put("d", 20);
            Object result = null;
            try {
                result = engine.eval(str);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
            System.out.println("结果类型:" + result.getClass().getName() + ",计算结果:" + result);
        }).start();


    }

}
