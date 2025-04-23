package com.example.friend.controller;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.PageQueryDTO;
import com.example.common.core.domain.PageResult;
import com.example.friend.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/user/message")
public class MessageController extends BaseController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/list")
    public PageResult getMessages(PageQueryDTO pageQueryDTO) {
        log.info("查询用户信息：{}", pageQueryDTO);
        processPageArgs(pageQueryDTO);
        return messageService.getMessages(pageQueryDTO);
    }

}
