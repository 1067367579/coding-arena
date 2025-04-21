package com.example.friend.service;

import com.example.common.core.domain.PageResult;
import com.example.friend.domain.dto.QuestionQueryDTO;
import com.example.friend.domain.vo.QuestionVO;

public interface QuestionService {
    PageResult list(QuestionQueryDTO questionQueryDTO);

    QuestionVO detail(Long questionId);

    Long next(Long questionId);

    Long pre(Long questionId);
}
