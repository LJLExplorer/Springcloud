package com.ljl.user.service.impl;

import com.ljl.user.domain.dto.User;
import com.ljl.user.mapper.UserMapper;
import com.ljl.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User selectByPrimaryKey(Long id) {
        return userMapper.queryById(id);
    }
}
