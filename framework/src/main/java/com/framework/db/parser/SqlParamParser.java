package com.framework.db.parser;

import java.lang.reflect.Parameter;

public interface SqlParamParser {

    SqlParamParserResult generateSql (String rawSql, Parameter[] parameters, Object[] args);

}
