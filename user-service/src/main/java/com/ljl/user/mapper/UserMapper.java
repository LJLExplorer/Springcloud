package com.ljl.user.mapper;

import com.ljl.user.domain.dto.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    User queryById(Long id);
}
