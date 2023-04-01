package com.dlut.community.util;

import com.dlut.community.pojo.User;
import org.springframework.stereotype.Component;

/*
* 持有用户信息，用于代替session对象
* */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    /*
    * 清理用户信息
    * */
    public void clear() {
        users.remove();
    }
}
