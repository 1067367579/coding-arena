package com.example.system.controller;

import com.example.core.controller.BaseController;
import com.example.core.domain.PageResult;
import com.example.core.domain.Result;
import com.example.system.domain.question.dto.QuestionAddDTO;
import com.example.system.domain.question.dto.QuestionEditDTO;
import com.example.system.domain.question.dto.QuestionQueryDTO;
import com.example.system.domain.question.vo.QuestionVO;
import com.example.system.service.QuestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sys/question")
@Tag(name = "题库管理接口")
@Slf4j
public class QuestionController extends BaseController {

    @Autowired
    QuestionService questionService;

    //列表 参数 题目难度 模糊查询 分页查询
    //需要告知前端总页数 新的数据结构 列表相关的接口都可以使用此数据结构返回
    //分多少页 由前端计算
    //用请求参数的方式传入参数
    @GetMapping("/list")
    public PageResult getQuestionList(QuestionQueryDTO questionQueryDTO) {
      log.info("列表查询参数:{}", questionQueryDTO);
      processPageArgs(questionQueryDTO);
      return getPageResult(questionService.getQuestionList(questionQueryDTO));
    }

    @PostMapping("/add")
    public Result addQuestion(@RequestBody @Validated QuestionAddDTO question) {
        log.info("新增题目:{}",question);
        return responseByService(questionService.addQuestion(question));
    }

    @GetMapping("/detail")
    public Result<QuestionVO> getQuestionDetail(Long questionId) {
        log.info("获取题目详细信息:{}",questionId);
        return questionService.getDetail(questionId);
    }

    @PutMapping("/edit")
    public Result editQuestion(@RequestBody @Validated QuestionEditDTO editDTO) {
        log.info("编辑题目:{}",editDTO);
        return responseByService(questionService.edit(editDTO));
    }

    @DeleteMapping("/delete")
    public Result deleteQuestion(Long questionId) {
        log.info("删除题目:{}",questionId);
        return responseByService(questionService.del(questionId));
    }
}
