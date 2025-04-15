package com.example.friend.service;

import com.example.core.domain.PageResult;
import com.example.friend.domain.dto.ExamQueryDTO;
import com.example.friend.domain.vo.ExamQueryVO;

import java.util.List;

public interface ExamService {
    List<ExamQueryVO> list(ExamQueryDTO examQueryDTO);

    //todo 这里采用了和课件不一样的逻辑
    PageResult redisList(ExamQueryDTO examQueryDTO);
}
