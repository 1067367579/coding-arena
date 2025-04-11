package com.example.system.service;

import com.example.system.domain.exam.dto.ExamQueryDTO;

import java.util.List;

public interface ExamService {
    List<?> list(ExamQueryDTO examQueryDTO);
}
