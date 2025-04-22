package com.example.friend.controller;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.PageResult;
import com.example.common.core.domain.Result;
import com.example.friend.annotation.CheckUserStatus;
import com.example.friend.domain.dto.ExamQueryDTO;
import com.example.friend.service.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/exam")
public class ExamController extends BaseController {

    @Autowired
    private ExamService examService;

    //C端竞赛列表查询
    //加上semiLogin标记放行该接口
    @GetMapping("/semiLogin/list")
    public PageResult list(ExamQueryDTO examQueryDTO) {
        log.info("列表查询竞赛:{}", examQueryDTO);
        processPageArgs(examQueryDTO);
        return getPageResult(examService.list(examQueryDTO));
    }

    //C端竞赛查询 redis版本
    @GetMapping("/semiLogin/redis/list")
    public PageResult redisList(ExamQueryDTO examQueryDTO) {
        log.info("列表查询竞赛redis:{}", examQueryDTO);
        processPageArgs(examQueryDTO);
        return examService.redisList(examQueryDTO);
    }

    //获取首道题的ID
    @GetMapping("/getFirstQuestion")
    public Result<String> getFirstQuestion(Long examId) {
        //获取竞赛题目的顺序列表 找到第一个的题目Id 返回
        return Result.ok(examService.getFirstQuestion(examId));
    }

    //切换题目
    @GetMapping("/pre")
    public Result<String> pre(Long examId,Long questionId) {
        return Result.ok(examService.preQuestion(examId,questionId));
    }

    @GetMapping("/next")
    public Result<String> next(Long examId,Long questionId) {
        return Result.ok(examService.nextQuestion(examId,questionId));
    }
}
