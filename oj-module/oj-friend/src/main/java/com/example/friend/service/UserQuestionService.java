package com.example.friend.service;

import com.example.api.domain.vo.UserQuestionResultVO;
import com.example.common.core.domain.Result;
import com.example.friend.domain.dto.UserSubmitDTO;

public interface UserQuestionService {
    Result<UserQuestionResultVO> submit(UserSubmitDTO userSubmitDTO);

    boolean rabbitSubmit(UserSubmitDTO userSubmitDTO);

    Result<UserQuestionResultVO> getResult(Long questionId, Long examId, String currentTime);
}
