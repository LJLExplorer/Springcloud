package com.ljl.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljl.user.domain.dto.User;
import com.ljl.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Value("${server.port}")
    private String port;

    @GetMapping("/{id}")
    public String queryById(@PathVariable Long id) throws JsonProcessingException {
        User user = userService.selectByPrimaryKey(id);
//        User user = new User();
//        user.setUserName("123");
        ObjectMapper mapper = new ObjectMapper();
        log.info("port: {}", port);
        return mapper.writeValueAsString(user);
    }
}
