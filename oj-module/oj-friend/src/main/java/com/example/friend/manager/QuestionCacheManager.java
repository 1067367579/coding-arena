package com.example.friend.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.enums.ResultCode;
import com.example.common.redis.service.RedisService;
import com.example.common.security.exception.ServiceException;
import com.example.friend.domain.entity.Question;
import com.example.friend.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class QuestionCacheManager {

    @Autowired
    private RedisService redisService;

    @Autowired
    private QuestionMapper questionMapper;

    public Long listSize() {
        return redisService.getListSize(CacheConstants.QUESTION_LIST_KEY);
    }

    public void refreshQuestionCache() {
        //从数据库中查找
        List<Question> questions =
                questionMapper.selectList(new LambdaQueryWrapper<Question>().select(Question::getQuestionId)
                        .orderByDesc(Question::getCreateTime));
        //如果没有直接退出
        if (CollectionUtils.isEmpty(questions)) {
            return;
        }
        List<Long> questionIds = questions.stream().map(Question::getQuestionId).toList();
        //删除
        redisService.deleteObject(CacheConstants.QUESTION_LIST_KEY);
        //有 刷新缓存
        redisService.rightPushAll(CacheConstants.QUESTION_LIST_KEY, questionIds);
    }

    //获取上一道题
    public Long preQuestion(Long questionId) {
        Long index = redisService.indexOfForList(CacheConstants.QUESTION_LIST_KEY,questionId);
        if(index == 0) {
            throw new ServiceException(ResultCode.FAILED_FIRST_QUESTION);
        }
        // 如果不是 获取到前一个的questionId 返回
        return (Long)redisService.indexOf(CacheConstants.QUESTION_LIST_KEY,index-1);
    }

    //获取下一道题
    public Long nextQuestion(Long questionId) {
        Long index = redisService.indexOfForList(CacheConstants.QUESTION_LIST_KEY,questionId);
        //获取总长度
        Long size = listSize();
        if(index == size-1) {
            throw new ServiceException(ResultCode.FAILED_LAST_QUESTION);
        }
        // 如果不是 获取到前一个的questionId 返回
        return (Long)redisService.indexOf(CacheConstants.QUESTION_LIST_KEY,index+1);
    }
}
