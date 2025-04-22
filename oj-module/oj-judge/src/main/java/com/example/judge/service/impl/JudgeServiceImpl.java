package com.example.judge.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.api.domain.UserExeResult;
import com.example.api.domain.dto.JudgeDTO;
import com.example.api.domain.vo.UserQuestionResultVO;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.constants.JudgeConstants;
import com.example.common.core.domain.Result;
import com.example.common.core.enums.CodeRunStatus;
import com.example.common.redis.service.RedisService;
import com.example.judge.domain.entity.UserSubmit;
import com.example.judge.domain.result.SandboxExecuteResult;
import com.example.judge.mapper.UserSubmitMapper;
import com.example.judge.service.JudgeService;
import com.example.judge.service.SandboxPoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class JudgeServiceImpl implements JudgeService {

    @Autowired
    SandboxPoolService sandboxPoolService;

    @Autowired
    UserSubmitMapper userSubmitMapper;
    @Autowired
    private RedisService redisService;

    @Transactional
    @Override
    public Result<UserQuestionResultVO> doJudgeJavaCode(JudgeDTO judgeDTO) {
        //沙盒服务执行结果返回
        SandboxExecuteResult exeResult = sandboxPoolService.exeJavaCode(judgeDTO.getUserId(), judgeDTO.getUserCode(),judgeDTO.getInputList());
        //创建VO对象
        UserQuestionResultVO resultVO = new UserQuestionResultVO();
        //拿到返回值之后 先去对结果进行判断
        if(exeResult!=null && CodeRunStatus.SUCCEED.equals(exeResult.getRunStatus())) {
            judge(judgeDTO, exeResult, resultVO);
        } else {
            //处理编译运行错误的VO
            handleRunError(resultVO, exeResult);
        }
        saveUserSubmit(judgeDTO, resultVO);
        return Result.ok(resultVO);
    }

    private static void handleRunError(UserQuestionResultVO resultVO, SandboxExecuteResult exeResult) {
        //编译运行失败 返回
        resultVO.setPass(JudgeConstants.FALSE);
        if(exeResult != null) {
            //说明是运行错误 获取到运行错误的信息
            resultVO.setExeMessage(exeResult.getExeMessage());
        } else {
            //如果连结果都是空 自己写错误信息
            resultVO.setExeMessage(JudgeConstants.UNKNOWN_ERROR);
        }
        //设置分数为0分
        resultVO.setScore(JudgeConstants.ERROR_SCORE);
    }

    private void saveUserSubmit(JudgeDTO judgeDTO, UserQuestionResultVO resultVO) {
        //删除数据库中原有的记录
        int result = userSubmitMapper.delete(new LambdaQueryWrapper<UserSubmit>()
                .eq(UserSubmit::getUserId, judgeDTO.getUserId())
                .eq(UserSubmit::getQuestionId, judgeDTO.getQuestionId())
                .eq(judgeDTO.getExamId()!=null,
                        UserSubmit::getExamId, judgeDTO.getExamId())
        );
        //只有是用户第一次答题，热榜才作数，避免刷榜，数据库redis不一致的问题
        if(result == 0) {
            redisService.zSetIncrementScoreByOne(CacheConstants.HOT_QUESTION_LIST_KEY,judgeDTO.getQuestionId());
        }
        //存入数据库中
        UserSubmit userSubmit = new UserSubmit();
        userSubmit.setUserCode(judgeDTO.getUserCode());
        userSubmit.setUserId(judgeDTO.getUserId());
        userSubmit.setQuestionId(judgeDTO.getQuestionId());
        userSubmit.setExamId(judgeDTO.getExamId());
        userSubmit.setPass(resultVO.getPass());
        userSubmit.setScore(resultVO.getScore());
        userSubmit.setProgramType(judgeDTO.getProgramType());
        userSubmit.setExeMessage(resultVO.getExeMessage());
        userSubmit.setCaseJudgeRes(JSON.toJSONString(resultVO.getUserExeResultList()));
        userSubmitMapper.insert(userSubmit);
    }

    private static void judge(JudgeDTO judgeDTO, SandboxExecuteResult exeResult, UserQuestionResultVO resultVO) {
        //运行成功 执行下一步判断 判断结果
        List<String> exeOutputList = exeResult.getOutputList();
        List<String> inputList = judgeDTO.getInputList();
        List<String> outputList = judgeDTO.getOutputList();
        if(exeOutputList!=null &&
                exeOutputList.size()!=inputList.size()) {
            handlePartlyError(resultVO);
        } else {
            boolean pass = true;
            List<UserExeResult> resultList = new ArrayList<>();
            pass = resultCompare(outputList, exeOutputList, inputList, pass, resultList);
            if(pass) {
                checkMemoryAndTime(judgeDTO, exeResult, resultVO, resultList);
            } else {
                //比对结果不通过
                handlePartlyError(resultVO);
                resultVO.setUserExeResultList(resultList);
            }
        }
    }

    private static void handlePartlyError(UserQuestionResultVO resultVO) {
        resultVO.setPass(JudgeConstants.FALSE);
        resultVO.setScore(JudgeConstants.ERROR_SCORE);
        resultVO.setExeMessage(JudgeConstants.ERROR_ANSWER);
    }

    private static void checkMemoryAndTime(JudgeDTO judgeDTO, SandboxExecuteResult exeResult, UserQuestionResultVO resultVO, List<UserExeResult> resultList) {
        //比对结果成功 接下来看时间空间
        if(exeResult.getUseMemory() > judgeDTO.getSpaceLimit()){
            resultVO.setPass(JudgeConstants.FALSE);
            resultVO.setScore(JudgeConstants.ERROR_SCORE);
            resultVO.setExeMessage(JudgeConstants.OUT_OF_MEMORY);
            return;
        }
        if(exeResult.getUseTime() > judgeDTO.getSpaceLimit()){
            resultVO.setPass(JudgeConstants.FALSE);
            resultVO.setScore(JudgeConstants.ERROR_SCORE);
            resultVO.setExeMessage(JudgeConstants.OUT_OF_TIME);
            return;
        }
        //时间空间通过
        resultVO.setPass(JudgeConstants.TRUE);
        resultVO.setScore(JudgeConstants.DEFAULT_SCORE*
                judgeDTO.getDifficulty());
        resultVO.setExeMessage(JudgeConstants.PASSED);
        resultVO.setUserExeResultList(resultList);
    }

    private static boolean resultCompare(List<String> outputList, List<String> exeOutputList, List<String> inputList, boolean pass, List<UserExeResult> resultList) {
        for (int i = 0; i < outputList.size(); i++) {
            String output = outputList.get(i);
            String exeOutput = exeOutputList.get(i);
            String input = inputList.get(i);
            if(!output.equals(exeOutput)) {
                pass = false;
            }
            UserExeResult result = new UserExeResult();
            result.setInput(input);
            result.setOutput(output);
            result.setExeOutput(exeOutput);
            resultList.add(result);
        }
        return pass;
    }
}
