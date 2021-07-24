package com.main.mapper;

import com.framework.annotation.db.EnableCache;
import com.framework.annotation.db.Mapper;
import com.framework.annotation.db.Param;
import com.framework.annotation.db.Query;
import com.main.entity.TestTable;

import java.util.List;

@Mapper
@EnableCache
public interface TestOneMapper {

    @Query(sql = "select * from test_table where id > #{id}")
    List<TestTable> getResult(@Param(value = "id") Integer id);

}
