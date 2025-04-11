package com.example.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.security.exception.ServiceException;
import com.example.core.enums.ResultCode;
import com.example.system.domain.question.Question;
import com.example.system.domain.question.dto.QuestionAddDTO;
import com.example.system.domain.question.dto.QuestionQueryDTO;
import com.example.system.domain.question.vo.QuestionQueryVO;
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
        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<>(Question.class)
                .eq(Question::getTitle, questionAddDTO.getTitle()));
        if(!CollectionUtils.isEmpty(questions)){
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        Question question = new Question();
        BeanUtil.copyProperties(questionAddDTO,question);
        return questionMapper.insert(question);
    }
}
