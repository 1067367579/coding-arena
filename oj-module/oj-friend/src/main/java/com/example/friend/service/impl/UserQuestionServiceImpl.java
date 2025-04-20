package com.example.friend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.example.api.domain.dto.JudgeDTO;
import com.example.api.domain.vo.UserQuestionResultVO;
import com.example.api.service.RemoteJudgeService;
import com.example.common.core.constants.JwtConstants;
import com.example.common.core.domain.Result;
import com.example.common.core.enums.ProgramType;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.common.security.exception.ServiceException;
import com.example.friend.domain.QuestionCase;
import com.example.friend.domain.dto.UserSubmitDTO;
import com.example.friend.domain.entity.Question;
import com.example.friend.domain.entity.QuestionES;
import com.example.friend.elasticsearch.QuestionRepository;
import com.example.friend.mapper.QuestionMapper;
import com.example.friend.service.UserQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserQuestionServiceImpl implements UserQuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private RemoteJudgeService remoteJudgeService;

    @Override
    public Result<UserQuestionResultVO> submit(UserSubmitDTO userSubmitDTO) {
        //检查参数 语言
        if(!ProgramType.JAVA.getValue().equals(userSubmitDTO.getProgramType())) {
            throw new ServiceException(ResultCode.FAILED_LANGUAGE_NOT_SUPPORTED);
        }
        JudgeDTO judgeDTO = assembleJudgeDTO(userSubmitDTO);
        return remoteJudgeService.doJudgeJavaCode(judgeDTO);
    }

    public JudgeDTO assembleJudgeDTO(UserSubmitDTO userSubmitDTO) {
        Long userId = (Long) ThreadLocalUtil.getLocalMap().get(JwtConstants.USER_ID);
        //从ES中获取main函数代码
        QuestionES questionES = questionRepository.findById(userSubmitDTO.getQuestionId())
                .orElse(null);
        if(questionES == null) {
            //刷新ES
            Question question = questionMapper.selectById(userSubmitDTO.getQuestionId());
            if(question == null) {
                throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
            }
            questionES = BeanUtil.toBean(question, QuestionES.class);
        }
        //获取出来了问题的详细数据 进行拼接
        String mainFunc = questionES.getMainFunc();
        String execCode = connectMainFuc(userSubmitDTO.getUserCode(),mainFunc);
        //解析获取出来的输入输出JSON字符串 化作列表
        List<QuestionCase> questionCases = JSON.parseArray(questionES.getQuestionCase(), QuestionCase.class);
        List<String> inputs = questionCases.stream().map(QuestionCase::getInput).toList();
        List<String> outputs = questionCases.stream().map(QuestionCase::getOutput).toList();
        JudgeDTO judgeDTO = new JudgeDTO();
        judgeDTO.setUserCode(execCode);
        judgeDTO.setQuestionId(questionES.getQuestionId());
        judgeDTO.setDifficulty(questionES.getDifficulty());
        judgeDTO.setExamId(userSubmitDTO.getExamId());
        judgeDTO.setUserId(userId);
        judgeDTO.setInputList(inputs);
        judgeDTO.setOutputList(outputs);
        judgeDTO.setTimeLimit(questionES.getTimeLimit());
        judgeDTO.setSpaceLimit(questionES.getSpaceLimit());
        return judgeDTO;
    }

    public String connectMainFuc(String userCode,String mainFunc) {
        String targetCharacter = "}";
        int targetLastIndex = userCode.lastIndexOf(targetCharacter);
        if (targetLastIndex != -1) {
            return userCode.substring(0, targetLastIndex) + "\n" + mainFunc + "\n" + userCode.substring(targetLastIndex);
        }
        throw new ServiceException(ResultCode.FAILED);
    }
}
