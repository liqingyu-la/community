package com.lqy.community.service;

import com.lqy.community.dao.UserMapper;
import com.lqy.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUserById(int userId){
        return userMapper.selectById(userId);
    }
}
