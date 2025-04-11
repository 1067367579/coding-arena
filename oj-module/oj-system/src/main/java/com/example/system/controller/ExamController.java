package com.example.system.controller;

import com.example.core.controller.BaseController;
import com.example.core.domain.PageResult;
import com.example.core.domain.Result;
import com.example.system.domain.exam.dto.ExamQueryDTO;
import com.example.system.domain.exam.vo.ExamQueryVO;
import com.example.system.service.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/sys/exam")
public class ExamController extends BaseController {

    @Autowired
    private ExamService examService;

    @GetMapping("/list")
    public PageResult list(ExamQueryDTO examQueryDTO) {
        log.info("列表查询竞赛:{}", examQueryDTO);
        processPageArgs(examQueryDTO);
        return getPageResult(examService.list(examQueryDTO));
    }
}
