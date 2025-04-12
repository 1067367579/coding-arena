package com.example.system.service;

import com.example.core.domain.Result;
import com.example.system.domain.exam.dto.ExamAddDTO;
import com.example.system.domain.exam.dto.ExamEditDTO;
import com.example.system.domain.exam.dto.ExamQueryDTO;
import com.example.system.domain.exam.dto.ExamQuestionDTO;
import com.example.system.domain.exam.vo.ExamAddVO;
import com.example.system.domain.exam.vo.ExamVO;

import java.util.List;

public interface ExamService {
    List<?> list(ExamQueryDTO examQueryDTO);

    ExamAddVO add(ExamAddDTO examAddDTO);

    boolean addQuestion(ExamQuestionDTO dto);

    ExamVO detail(Long examId);

    int edit(ExamEditDTO examEditDTO);
}
