package com.ljl.root.mapper;

import com.ljl.root.domain.dto.Root;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RootMapper {
    Root queryById(Long id);
}
