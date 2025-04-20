package com.example.judge.service;

import com.example.api.domain.dto.JudgeDTO;
import com.example.api.domain.vo.UserQuestionResultVO;
import com.example.common.core.domain.Result;

public interface JudgeService {
    Result<UserQuestionResultVO> doJudgeJavaCode(JudgeDTO judgeDTO);
}
