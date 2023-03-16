package com.dlut.community.Service;

import com.dlut.community.dao.UserMapper;
import com.dlut.community.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserMapper userMapper;
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }
}
