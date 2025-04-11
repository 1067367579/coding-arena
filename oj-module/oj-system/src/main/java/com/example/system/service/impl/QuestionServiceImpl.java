package com.example.system.service.impl;

import com.example.system.domain.question.dto.QuestionQueryDTO;
import com.example.system.domain.question.vo.QuestionQueryVO;
import com.example.system.mapper.QuestionMapper;
import com.example.system.service.QuestionService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
