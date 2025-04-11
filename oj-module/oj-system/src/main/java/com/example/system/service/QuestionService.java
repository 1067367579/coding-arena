package com.example.system.service;

import com.example.system.domain.question.dto.QuestionQueryDTO;

import java.util.List;

public interface QuestionService {
    List<?> getQuestionList(QuestionQueryDTO questionQueryDTO);
}
