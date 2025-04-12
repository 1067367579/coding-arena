package com.example.system.service;

import com.example.system.domain.exam.dto.ExamAddDTO;
import com.example.system.domain.exam.dto.ExamQueryDTO;
import com.example.system.domain.exam.dto.ExamQuestionDTO;

import java.util.List;

public interface ExamService {
    List<?> list(ExamQueryDTO examQueryDTO);

    int add(ExamAddDTO examAddDTO);

    boolean addQuestion(ExamQuestionDTO dto);
}
