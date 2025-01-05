package com.ljl.consumer.client;

import com.ljl.consumer.domain.dto.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    //http://user-service/user/123
    @GetMapping("/user/{id}")
    String queryById(@PathVariable("id") Long id);
}
