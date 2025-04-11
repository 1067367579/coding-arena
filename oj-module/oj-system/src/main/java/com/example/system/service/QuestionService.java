package com.example.system.service;

import com.example.core.domain.Result;
import com.example.system.domain.question.dto.QuestionAddDTO;
import com.example.system.domain.question.dto.QuestionEditDTO;
import com.example.system.domain.question.dto.QuestionQueryDTO;
import com.example.system.domain.question.vo.QuestionVO;

import java.util.List;

public interface QuestionService {
    List<?> getQuestionList(QuestionQueryDTO questionQueryDTO);

    int addQuestion(QuestionAddDTO question);

    Result<QuestionVO> getDetail(Long questionId);

    int edit(QuestionEditDTO editDTO);
}
