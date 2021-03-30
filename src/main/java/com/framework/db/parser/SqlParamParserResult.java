package com.framework.db.parser;

public class SqlParamParserResult {

    String desSql;
    Object[] sqlArgs;
    String[] jdbcTypes;

    public String getDesSql() {
        return desSql;
    }

    public void setDesSql(String desSql) {
        this.desSql = desSql;
    }

    public Object[] getSqlArgs() {
        return sqlArgs;
    }

    public void setSqlArgs(Object[] sqlArgs) {
        this.sqlArgs = sqlArgs;
    }

    public String[] getJdbcTypes() {
        return jdbcTypes;
    }

    public void setJdbcTypes(String[] jdbcTypes) {
        this.jdbcTypes = jdbcTypes;
    }
}
