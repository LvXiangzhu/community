package com.dlut.community.dao;

import com.dlut.community.pojo.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {
    //插入一条数据
    @Insert({
            "Insert into login_ticket (user_id, ticket, status, expired) ", //为了拼接不出错，最好每行结尾加空格
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    //注解方式需要额外配置主键自动生成
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    //根据ticket查询数据
    @Select({
            "select id, user_id, ticket, status, expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    //修改凭证状态
    //注解也能实现动态sql
    //里面的双引号需要用\转义
    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\"> ",
            "and 1=1 ",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);
}
