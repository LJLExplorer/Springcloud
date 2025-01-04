package com.ljl.user.service;

import com.ljl.user.domain.dto.User;

public interface UserService {
    User selectByPrimaryKey(Long id);
}
