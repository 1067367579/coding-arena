package com.example.judge.controller;

import com.example.api.domain.dto.JudgeDTO;
import com.example.api.domain.vo.UserQuestionResultVO;
import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.Result;
import com.example.judge.service.JudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/judge")
public class JudgeController extends BaseController {

    @Autowired
    private JudgeService judgeService;

    @PostMapping("/doJudgeJavaCode")
    Result<UserQuestionResultVO> doJudgeJavaCode(@RequestBody JudgeDTO judgeDTO) {
        log.info("判断Java代码: {}", judgeDTO);
        return judgeService.doJudgeJavaCode(judgeDTO);
    }
}
