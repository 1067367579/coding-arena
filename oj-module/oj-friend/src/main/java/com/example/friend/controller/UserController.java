package com.example.friend.controller;

import com.example.core.controller.BaseController;
import com.example.core.domain.Result;
import com.example.friend.domain.dto.SendCodeDTO;
import com.example.friend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @PostMapping("/code")
    public Result<?> sendCode(@RequestBody SendCodeDTO sendCodeDTO) {
        log.info("根据邮箱号发送短信验证码:{}", sendCodeDTO);
        return userService.sendCode(sendCodeDTO);
    }
}
