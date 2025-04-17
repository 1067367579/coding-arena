package com.example.system.manager;

import com.example.common.redis.service.RedisService;
import com.example.core.constants.CacheConstants;
import com.example.system.domain.exam.entity.Exam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//竞赛缓存管理类 管理员端
@Component
public class ExamCacheManager {

    @Autowired
    private RedisService redisService;

    public void addCache(Exam exam) {
        //将examId存入列表中即可 后续取的话再拼装为真实的key取出对应的数据
        redisService.leftPushForList(getExamListKey(),exam.getExamId());
        redisService.setCacheObject(getDetailKey(exam.getExamId()),exam);
    }

    public void deleteCache(Long examId) {
        //将当前examId对应的竞赛信息从未完赛列表中移除
        redisService.removeForList(getExamListKey(),examId);
        //移除具体竞赛信息的缓存
        redisService.deleteObject(getDetailKey(examId));
    }

    //管理端只能对未完赛的竞赛进行发布 或者 撤销发布的操作 历史竞赛无法操作
    public String getExamListKey() {
        return CacheConstants.EXAM_UNFINISHED_LIST_KEY;
    }

    //获取redis中的exam数据对应的key
    public String getDetailKey(Long examId) {
        return examId + CacheConstants.EXAM_DETAIL_KEY_PREFIX;
    }

    //已结束的竞赛进行处理 移动缓存 使用xxl-job重新刷新缓存定时处理
}
