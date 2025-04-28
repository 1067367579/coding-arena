package com.example.friend.service;

import com.example.common.core.domain.PageResult;
import com.example.friend.domain.dto.ExamQueryDTO;
import com.example.friend.domain.dto.UserExamDTO;

public interface UserExamService {
    int enter(UserExamDTO userExamDTO);

    PageResult list(ExamQueryDTO queryDTO);
}
