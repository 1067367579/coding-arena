package com.example.system.controller;

import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.PageResult;
import com.example.common.core.domain.Result;
import com.example.system.domain.exam.dto.ExamAddDTO;
import com.example.system.domain.exam.dto.ExamEditDTO;
import com.example.system.domain.exam.dto.ExamQueryDTO;
import com.example.system.domain.exam.dto.ExamQuestionDTO;
import com.example.system.domain.exam.vo.ExamAddVO;
import com.example.system.domain.exam.vo.ExamVO;
import com.example.system.service.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/add")
    public Result<ExamAddVO> add(@RequestBody ExamAddDTO examAddDTO) {
        log.info("新增竞赛:{}", examAddDTO);
        return Result.ok(examService.add(examAddDTO));
    }

    @PostMapping("/question/add")
    public Result addQuestion(@RequestBody ExamQuestionDTO examQuestionDTO) {
        log.info("新增题目到竞赛中:{}", examQuestionDTO);
        return responseByService(examService.addQuestion(examQuestionDTO));
    }

    @GetMapping("/detail")
    public Result<ExamVO> detail(Long examId) {
        log.info("查看竞赛详细信息:{}",examId);
        return Result.ok(examService.detail(examId));
    }

    @PutMapping("/edit")
    public Result<?> edit(@RequestBody ExamEditDTO examEditDTO) {
        log.info("编辑竞赛基本信息:{}",examEditDTO);
        return responseByService(examService.edit(examEditDTO));
    }

    @DeleteMapping("/question/delete")
    public Result<?> deleteQuestion(Long examId,Long questionId) {
        log.info("删除竞赛中的题目，竞赛ID：{},题目ID:{}",examId,questionId);
        return responseByService(examService.deleteQuestion(examId,questionId));
    }

    @DeleteMapping("/delete")
    public Result<?> delete(Long examId) {
        log.info("删除竞赛：{}",examId);
        return responseByService(examService.delete(examId));
    }

    @PutMapping("/publish")
    public Result<?> publish(Long examId) {
        log.info("发布竞赛：{}",examId);
        return responseByService(examService.publish(examId));
    }

    @PutMapping("/publish/cancel")
    public Result<?> publishCancel(Long examId) {
        log.info("撤销发布：{}",examId);
        return responseByService(examService.cancelPublish(examId));
    }
}
