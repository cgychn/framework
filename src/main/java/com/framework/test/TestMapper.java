package com.framework.test;

import com.framework.annotation.Mapper;
import com.framework.annotation.Modifying;
import com.framework.annotation.Param;
import com.framework.annotation.Query;

import java.util.List;

@Mapper
public interface TestMapper {

    @Query(sql = "select user_name as userName, login_name as loginName, password from t_user where user_name like #{userName}")
    List<User> getUsers(@Param("userName") String userName);

    @Modifying
    @Query(sql = "insert into t_user (login_name, password) values (#{userName}, #{pwd})")
    void addUser(@Param(value = "userName") String userName, @Param(value = "pwd") String pwd);

}
