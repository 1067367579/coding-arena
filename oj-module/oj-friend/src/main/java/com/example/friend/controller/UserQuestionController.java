package com.example.friend.controller;

import com.example.api.domain.vo.UserQuestionResultVO;
import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.Result;
import com.example.friend.domain.dto.UserSubmitDTO;
import com.example.friend.domain.vo.QuestionQueryVO;
import com.example.friend.domain.vo.QuestionVO;
import com.example.friend.service.UserQuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user/question")
public class UserQuestionController extends BaseController {

    @Autowired
    private UserQuestionService userQuestionService;

    @PostMapping("/submit")
    public Result<UserQuestionResultVO> submit(@RequestBody UserSubmitDTO userSubmitDTO) {
        log.info("提交代码：{}", userSubmitDTO);
        return userQuestionService.submit(userSubmitDTO);
    }

    @PostMapping("/rabbit/submit")
    public Result<?> rabbitSubmit(@RequestBody UserSubmitDTO userSubmitDTO) {
        log.info("提交代码RabbitMQ：{}", userSubmitDTO);
        return responseByService(userQuestionService.rabbitSubmit(userSubmitDTO));
    }

    @GetMapping("/exe/result")
    public Result<UserQuestionResultVO> result(Long questionId,Long examId,String currentTime) {
        log.info("问题：{},竞赛：{},提交时间:{}", questionId, examId, currentTime);
        return userQuestionService.getResult(questionId,examId,currentTime);
    }

    @GetMapping("/hot")
    public Result<List<QuestionQueryVO>> hotQuestions(Integer top) {
        log.info("找到热榜，热榜长度:{}",top);
        return userQuestionService.hotQuestions(top);
    }
}
