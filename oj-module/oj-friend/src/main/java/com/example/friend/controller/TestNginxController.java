package com.example.friend.controller;

import com.example.common.core.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/test/nginx")
public class TestNginxController {

    @GetMapping
    public Result<?> testNginx() {
        log.info("负载均衡测试");
        return Result.ok();
    }
}
