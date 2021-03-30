package com.framework.db.parser;

import java.lang.reflect.Parameter;

public interface SqlParamParser {

    String generateSql (String rawSql, Parameter[] parameters, Object[] args);

}
