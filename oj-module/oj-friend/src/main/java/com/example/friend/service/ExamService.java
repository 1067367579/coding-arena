package com.example.friend.service;

import com.example.common.core.domain.PageResult;
import com.example.friend.domain.dto.ExamQueryDTO;
import com.example.friend.domain.dto.ExamRankDTO;
import com.example.friend.domain.vo.ExamQueryVO;

import java.util.List;

public interface ExamService {
    List<ExamQueryVO> list(ExamQueryDTO examQueryDTO);

    PageResult redisList(ExamQueryDTO examQueryDTO);

    String getFirstQuestion(Long examId);

    String preQuestion(Long examId, Long questionId);

    String nextQuestion(Long examId, Long questionId);

    PageResult rankList(ExamRankDTO examRankDTO);
}
