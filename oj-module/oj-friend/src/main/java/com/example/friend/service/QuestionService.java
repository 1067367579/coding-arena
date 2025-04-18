package com.example.friend.service;

import com.example.core.domain.PageResult;
import com.example.friend.domain.dto.QuestionQueryDTO;

import java.util.List;

public interface QuestionService {
    PageResult list(QuestionQueryDTO questionQueryDTO);
}
