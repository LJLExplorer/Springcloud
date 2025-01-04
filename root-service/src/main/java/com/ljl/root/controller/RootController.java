package com.ljl.root.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljl.root.domain.dto.Root;
import com.ljl.root.service.RootService;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/root")
public class RootController {

    private final Logger logger = LoggerFactory.getLogger(RootService.class);

    @Value("${server.port}")
    private String port;

    @Resource
    private RootService rootService;

    @GetMapping("/{id}")
    private String queryById(@PathVariable Long id) throws JsonProcessingException {
        Root root = rootService.selectByPrimaryKey(id);
        ObjectMapper mapper = new ObjectMapper();
        logger.info("port: {}", port);
        return mapper.writeValueAsString(root);}
}
