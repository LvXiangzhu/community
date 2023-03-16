package com.dlut.community.dao;

import com.dlut.community.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper //也可以写@Repository，让Spring容器装配该Bean
public interface UserMapper {

    //注解方式虽然简单，但当SQL有变化时都需要重新编译代码，所以不建议用

//    @Select("select id, username, password, salt, email, type, status, activation_code, " +
//            "head_url, creat_time from user where id = #{id}")
    User selectById(Integer id);

//    @Select("select id, username, password, salt, email, type, status, activation_code, " +
//            "head_url, creat_time from user where username = #{username}")
    User selectByName(String username);

//    @Select("select id, username, password, salt, email, type, status, activation_code, " +
//            "head_url, creat_time from user where email = #{email}")
    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(Integer id, Integer status);

    int updateHeader(Integer id, String headerUrl);

    int updatePassword(Integer id, String password);
}
