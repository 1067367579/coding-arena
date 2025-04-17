package com.example.friend.service;

import com.example.core.domain.PageResult;
import com.example.friend.domain.dto.ExamQueryDTO;
import com.example.friend.domain.vo.ExamQueryVO;

import java.util.List;

public interface ExamService {
    List<ExamQueryVO> list(ExamQueryDTO examQueryDTO);

    PageResult redisList(ExamQueryDTO examQueryDTO);
}
