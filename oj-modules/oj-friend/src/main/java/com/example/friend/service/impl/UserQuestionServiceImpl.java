package com.example.friend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.api.domain.UserExeResult;
import com.example.api.domain.dto.JudgeDTO;
import com.example.api.domain.vo.UserQuestionResultVO;
import com.example.api.service.RemoteJudgeService;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.constants.JwtConstants;
import com.example.common.core.domain.Result;
import com.example.common.core.enums.PassStatus;
import com.example.common.core.enums.ProgramType;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.common.redis.service.RedisService;
import com.example.common.security.exception.ServiceException;
import com.example.friend.domain.QuestionCase;
import com.example.friend.domain.QuestionSubmit;
import com.example.friend.domain.dto.UserSubmitDTO;
import com.example.friend.domain.entity.Question;
import com.example.friend.domain.entity.QuestionES;
import com.example.friend.domain.entity.UserSubmit;
import com.example.friend.domain.vo.QuestionQueryVO;
import com.example.friend.elasticsearch.QuestionRepository;
import com.example.friend.mapper.QuestionMapper;
import com.example.friend.mapper.UserQuestionMapper;
import com.example.friend.mapper.UserSubmitMapper;
import com.example.friend.rabbit.JudgeProducer;
import com.example.friend.service.UserQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class UserQuestionServiceImpl implements UserQuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private RemoteJudgeService remoteJudgeService;

    @Autowired
    private JudgeProducer judgeProducer;

    @Autowired
    private UserSubmitMapper userSubmitMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserQuestionMapper userQuestionMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result<UserQuestionResultVO> submit(UserSubmitDTO userSubmitDTO) {
        //检查参数 语言
        if(!ProgramType.JAVA.getValue().equals(userSubmitDTO.getProgramType())) {
            throw new ServiceException(ResultCode.FAILED_LANGUAGE_NOT_SUPPORTED);
        }
        JudgeDTO judgeDTO = assembleJudgeDTO(userSubmitDTO);
        String submitKey = getSubmitKey(judgeDTO);
        stringRedisTemplate.opsForValue().set(submitKey, "",15L, TimeUnit.SECONDS);
        return remoteJudgeService.doJudgeJavaCode(judgeDTO);
    }

    public String getSubmitKey(JudgeDTO judgeDTO) {
        if(judgeDTO.getExamId() == null) {
            return CacheConstants.SUBMIT_KEY_PREFIX+judgeDTO.getUserId()
                    +":"+judgeDTO.getQuestionId();
        }
        return CacheConstants.SUBMIT_KEY_PREFIX+judgeDTO.getUserId()
                +":"+judgeDTO.getExamId()+":"+judgeDTO.getQuestionId();
    }

    @Override
    public boolean rabbitSubmit(UserSubmitDTO userSubmitDTO) {
        //检查参数 语言
        if(!ProgramType.JAVA.getValue().equals(userSubmitDTO.getProgramType())) {
            throw new ServiceException(ResultCode.FAILED_LANGUAGE_NOT_SUPPORTED);
        }
        JudgeDTO judgeDTO = assembleJudgeDTO(userSubmitDTO);
        String submitKey = getSubmitKey(judgeDTO);
        stringRedisTemplate.opsForValue().set(submitKey, "",15L, TimeUnit.SECONDS);
        judgeProducer.produceMessage(judgeDTO);
        return true;
    }

    @Override
    public Result<UserQuestionResultVO> getResult(Long questionId, Long examId, String currentTime) {
        //先拿用户ID
        Long userId = (Long) ThreadLocalUtil.getLocalMap().get(JwtConstants.USER_ID);
        //组装VO
        UserQuestionResultVO userQuestionResultVO = new UserQuestionResultVO();
        //先查redis中有无数据 注意缓存穿透
        JudgeDTO judgeDTO = new JudgeDTO();
        judgeDTO.setUserId(userId);
        judgeDTO.setExamId(examId);
        judgeDTO.setQuestionId(questionId);
        String submitKey = getSubmitKey(judgeDTO);
        String jsonStr = stringRedisTemplate.opsForValue().get(submitKey);
        if(jsonStr!=null) {
            if(jsonStr.isEmpty()) {
                //防止缓存穿透 直接返回正在处理中
                userQuestionResultVO.setPass(PassStatus.IN_JUDGE.getValue());
            } else {
                //redis中有值
                UserSubmit userSubmit = JSON.parseObject(jsonStr, UserSubmit.class);
                userQuestionResultVO.setPass(userSubmit.getPass());
                userQuestionResultVO.setExeMessage(userSubmit.getExeMessage());
                userQuestionResultVO.setScore(userSubmit.getScore());
                String caseJudgeRes = userSubmit.getCaseJudgeRes();
                if(StrUtil.isNotEmpty(caseJudgeRes)) {
                    userQuestionResultVO.setUserExeResultList(JSON.parseArray(userSubmit.getCaseJudgeRes(), UserExeResult.class));
                }
            }
            return Result.ok(userQuestionResultVO);
        }
        LocalDateTime submitTime = null;
        //通过用户ID，问题ID，竞赛ID唯一确定记录
        if(StrUtil.isNotEmpty(currentTime)) {
            submitTime = LocalDateTime.parse(currentTime, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        }
        //SQL 查询
        UserSubmit userSubmit = userSubmitMapper.selectOne(new LambdaQueryWrapper<UserSubmit>()
                .eq(UserSubmit::getUserId, userId)
                .eq(UserSubmit::getQuestionId, questionId)
                .eq(examId != null, UserSubmit::getExamId, examId)
                .gt(submitTime != null,UserSubmit::getCreateTime, submitTime)
        );
        if(userSubmit == null) {
            //没有结果，正在执行中
            stringRedisTemplate.opsForValue().set(submitKey, "",15L, TimeUnit.SECONDS);
            userQuestionResultVO.setPass(PassStatus.IN_JUDGE.getValue());
        } else {
            stringRedisTemplate.opsForValue().set(submitKey, JSON.toJSONString(userSubmit), 1L, TimeUnit.HOURS);
            userQuestionResultVO.setPass(userSubmit.getPass());
            userQuestionResultVO.setExeMessage(userSubmit.getExeMessage());
            userQuestionResultVO.setScore(userSubmit.getScore());
            String caseJudgeRes = userSubmit.getCaseJudgeRes();
            if(StrUtil.isNotEmpty(caseJudgeRes)) {
                userQuestionResultVO.setUserExeResultList(JSON.parseArray(userSubmit.getCaseJudgeRes(), UserExeResult.class));
            }
        }
        return Result.ok(userQuestionResultVO);
    }

    @Override
    public Result<List<QuestionQueryVO>> hotQuestions(Integer top) {
        Long size = redisService.zSetSize(CacheConstants.HOT_QUESTION_LIST_KEY);
        if(size == null || size == 0) {
            //数据库加载
            Set<ZSetOperations.TypedTuple<Long>> tuples = getTypedTuples();
            //加载成功
            redisService.zSetAddBatch(CacheConstants.HOT_QUESTION_LIST_KEY,tuples);
        }
        //获取
        Set<Long> questionIds = redisService.zRangeByList(CacheConstants.HOT_QUESTION_LIST_KEY, 0, top - 1, Long.class);
        //去数据库中拼装VO 小范围
        List<QuestionQueryVO> result = new ArrayList<>();
        for(Long questionId : questionIds) {
            result.add(questionMapper.getVOById(questionId));
        }
        return Result.ok(result);
    }

    private Set<ZSetOperations.TypedTuple<Long>> getTypedTuples() {
        List<QuestionSubmit> statistics = userSubmitMapper.getQuestionSubmitStatistics();
        //加载到zset里面
        Set<ZSetOperations.TypedTuple<Long>> tuples = new HashSet<>();
        for (QuestionSubmit statistic : statistics) {
            Long questionId = statistic.getQuestionId();
            Long count = statistic.getCount();
            ZSetOperations.TypedTuple<Long> tuple = new DefaultTypedTuple<>(questionId,count.doubleValue());
            tuples.add(tuple);
        }
        return tuples;
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
        judgeDTO.setProgramType(userSubmitDTO.getProgramType());
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
