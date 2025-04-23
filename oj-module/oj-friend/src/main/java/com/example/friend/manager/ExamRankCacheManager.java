package com.example.friend.manager;

import cn.hutool.core.collection.CollUtil;
import com.example.common.core.constants.CacheConstants;
import com.example.common.redis.service.RedisService;
import com.example.friend.domain.dto.ExamRankDTO;
import com.example.friend.domain.entity.UserScore;
import com.example.friend.domain.vo.ExamRankVO;
import com.example.friend.domain.vo.MessageTextVO;
import com.example.friend.mapper.UserExamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class ExamRankCacheManager {

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserExamMapper userExamMapper;


    public Long getListSize(Long examId) {
        return redisService.getListSize(getExamRankListKey(examId));
    }

    public void refreshCache(Long examId) {
        //从数据库中获取即可
        List<ExamRankVO> examRankList = userExamMapper.getExamRankList(examId);
        if (CollectionUtils.isEmpty(examRankList)) {
            return;
        }
        //删除原来的缓存 放到缓存当中
        String examRankListKey = getExamRankListKey(examId);
        redisService.deleteObject(examRankListKey);
        //加载新的缓存
        redisService.rightPushAll(examRankListKey, examRankList);
    }

    public String getExamRankListKey(Long examId) {
        return CacheConstants.EXAM_RANK_LIST_KEY_PREFIX+examId;
    }

    public List<ExamRankVO> getVOList(ExamRankDTO examRankDTO) {
        int start = (examRankDTO.getPageNum()-1) * examRankDTO.getPageSize();
        int end = start + examRankDTO.getPageSize()-1;
        String examRankListKey = getExamRankListKey(examRankDTO.getExamId());
        return redisService.getCacheListByRange(examRankListKey, start, end, ExamRankVO.class);
    }
}
