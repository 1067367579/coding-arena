package com.example.friend.controller;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.PageResult;
import com.example.common.core.domain.Result;
import com.example.friend.annotation.CheckUserStatus;
import com.example.friend.domain.dto.ExamQueryDTO;
import com.example.friend.domain.dto.UserExamDTO;
import com.example.friend.service.UserExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/user/exam")
public class UserExamController extends BaseController {

    @Autowired
    UserExamService userExamService;

    //新增报名信息 token存储了用户的信息 还有竞赛的信息需要传入
    @CheckUserStatus
    @PostMapping("/enter")
    public Result enter(@RequestBody UserExamDTO userExamDTO) {
        log.info("当前用户报名竞赛：{}", userExamDTO);
        return responseByService(userExamService.enter(userExamDTO));
    }

    @GetMapping("/list")
    public PageResult getExamList(ExamQueryDTO queryDTO) {
        log.info("查看用户报名的竞赛:{}", queryDTO);
        processPageArgs(queryDTO);
        return userExamService.list(queryDTO);
    }



}
