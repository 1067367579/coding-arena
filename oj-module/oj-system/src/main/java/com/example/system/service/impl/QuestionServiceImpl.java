package com.example.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.security.exception.ServiceException;
import com.example.core.domain.Result;
import com.example.core.enums.ResultCode;
import com.example.system.domain.question.Question;
import com.example.system.domain.question.dto.QuestionAddDTO;
import com.example.system.domain.question.dto.QuestionEditDTO;
import com.example.system.domain.question.dto.QuestionQueryDTO;
import com.example.system.domain.question.vo.QuestionQueryVO;
import com.example.system.domain.question.vo.QuestionVO;
import com.example.system.mapper.QuestionMapper;
import com.example.system.service.QuestionService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public List<QuestionQueryVO> getQuestionList(QuestionQueryDTO questionQueryDTO) {
        PageHelper.startPage(questionQueryDTO.getPageNum(), questionQueryDTO.getPageSize());
        return questionMapper.getQuestionList(questionQueryDTO);
    }

    @Override
    public int addQuestion(QuestionAddDTO questionAddDTO) {
        //判断题目的重复性
        isDuplicated(questionAddDTO.getTitle());
        Question question = new Question();
        BeanUtil.copyProperties(questionAddDTO,question);
        return questionMapper.insert(question);
    }

    private void isDuplicated(String title) {
        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<>(Question.class)
                .eq(Question::getTitle, title));
        if(!CollectionUtils.isEmpty(questions)){
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
    }

    @Override
    public Result<QuestionVO> getDetail(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if(question == null){
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtil.copyProperties(question,questionVO);
        return Result.ok(questionVO);
    }

    @Override
    public int edit(QuestionEditDTO editDTO) {
        //先校验题目ID
        Question question = questionMapper.selectById(editDTO.getQuestionId());
        if(question == null){
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        //查出来之后进行修改操作
        question.setTitle(editDTO.getTitle());
        question.setContent(editDTO.getContent());
        question.setQuestionCase(editDTO.getQuestionCase());
        question.setDifficulty(editDTO.getDifficulty());
        question.setDefaultCode(editDTO.getDefaultCode());
        question.setMainFunc(editDTO.getMainFunc());
        question.setTimeLimit(editDTO.getTimeLimit());
        question.setSpaceLimit(editDTO.getSpaceLimit());
        return questionMapper.updateById(question);
    }
}
