package com.example.friend.service;

import com.example.common.core.domain.PageResult;
import com.example.friend.domain.dto.QuestionQueryDTO;

public interface QuestionService {
    PageResult list(QuestionQueryDTO questionQueryDTO);
}
