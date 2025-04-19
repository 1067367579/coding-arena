package com.example.friend.controller;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.PageResult;
import com.example.friend.domain.dto.QuestionQueryDTO;
import com.example.friend.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/question")
public class QuestionController extends BaseController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/semiLogin/list")
    public PageResult list(QuestionQueryDTO questionQueryDTO) {
          log.info("查询题目列表:{}", questionQueryDTO);
          processPageArgs(questionQueryDTO);
          return questionService.list(questionQueryDTO);
    }
}
