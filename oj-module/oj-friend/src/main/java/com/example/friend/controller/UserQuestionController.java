package com.example.friend.controller;

import com.example.api.domain.vo.UserQuestionResultVO;
import com.example.common.core.domain.Result;
import com.example.friend.domain.dto.UserSubmitDTO;
import com.example.friend.service.UserQuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/user/question")
public class UserQuestionController {

    @Autowired
    private UserQuestionService userQuestionService;

    @PostMapping("/submit")
    public Result<UserQuestionResultVO> submit(@RequestBody UserSubmitDTO userSubmitDTO) {
        log.info("提交代码：{}", userSubmitDTO);
        return userQuestionService.submit(userSubmitDTO);
    }

}
