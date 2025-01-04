package com.ljl.root.service.impl;

import com.ljl.root.domain.dto.Root;
import com.ljl.root.mapper.RootMapper;
import com.ljl.root.service.RootService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class RootServiceImpl implements RootService {

    @Resource
    private RootMapper rootMapper;

    @Override
    public Root selectByPrimaryKey(Long id) {
        return rootMapper.queryById(id);
    }
}
