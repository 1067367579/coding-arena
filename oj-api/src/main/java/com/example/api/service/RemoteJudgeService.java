package com.example.api.service;

import com.example.api.domain.dto.JudgeDTO;
import com.example.api.domain.vo.UserQuestionResultVO;
import com.example.common.core.constants.Constants;
import com.example.common.core.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "RemoteJudgeService",value = Constants.JUDGE_SERVICE)
public interface RemoteJudgeService {

    @PostMapping("/judge/doJudgeJavaCode")
    Result<UserQuestionResultVO> doJudgeJavaCode(@RequestBody JudgeDTO judgeDTO);
}
