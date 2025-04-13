package com.example.system.controller;

import com.example.core.controller.BaseController;
import com.example.core.domain.PageResult;
import com.example.system.domain.user.dto.UserQueryDTO;
import com.example.system.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/sys/cuser")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public PageResult getUserList(UserQueryDTO userQueryDTO) {
        log.info("查看用户列表:{}", userQueryDTO);
        //校验分页参数 没有的话给出默认值
        processPageArgs(userQueryDTO);
        return getPageResult(userService.list(userQueryDTO));
    }
}
