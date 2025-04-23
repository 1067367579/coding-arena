package com.example.friend.controller;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.PageResult;
import com.example.common.core.domain.Result;
import com.example.friend.domain.dto.QuestionQueryDTO;
import com.example.friend.domain.vo.QuestionVO;
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

    //根据题目Id查询题目详情
    @GetMapping("/detail")
    public Result<QuestionVO> questionDetail(Long questionId) {
        log.info("查询题目详情:{}", questionId);
        return Result.ok(questionService.detail(questionId));
    }

    //上一题
    @GetMapping("/pre")
    public Result<String> preQuestion(Long questionId) {
        log.info("查找当前题目的上一题的ID:{}", questionId);
        return Result.ok(String.valueOf(questionService.pre(questionId)));
    }

    //下一题
    @GetMapping("/next")
    public Result<String> nextQuestion(Long questionId) {
        log.info("查找当前题目的下一题的ID:{}", questionId);
        return Result.ok(String.valueOf(questionService.next(questionId)));
    }
}
