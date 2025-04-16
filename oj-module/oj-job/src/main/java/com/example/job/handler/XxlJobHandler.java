package com.example.job.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.redis.service.RedisService;
import com.example.core.constants.CacheConstants;
import com.example.core.enums.ExamListType;
import com.example.job.domain.dto.ExamQueryDTO;
import com.example.job.domain.entity.Exam;
import com.example.job.domain.vo.ExamQueryVO;
import com.example.job.mapper.ExamMapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class XxlJobHandler {

    @Autowired
    RedisService redisService;

    @Autowired
    ExamMapper examMapper;

    @XxlJob("examListOrganizeHandler")
    public void examListOrganizeHandler(){
        log.info("执行定时刷新缓存操作～");
        //从数据库中查未完赛和历史竞赛的数据 刷新缓存
        refreshCache(0);
        refreshCache(1);
    }

    public void refreshCache(Integer type) {
        //数据库操作
        ExamQueryDTO examQueryDTO = new ExamQueryDTO();
        examQueryDTO.setType(type);
        List<ExamQueryVO> examList = examMapper.getExamList(examQueryDTO);

        //如果数据库中没有数据 直接返回 无需后续操作
        if(CollectionUtils.isEmpty(examList)) {
            return;
        }
        //有数据 创建一个HashMap 批量插入exam examId到list结构中
        Map<String, ExamQueryVO> examMap = new HashMap<>();
        List<Long> examIds = new ArrayList<>();
        //遍历返回数据
        for (ExamQueryVO vo : examList) {
            examMap.put(getDetailKey(vo.getExamId()),vo);
            examIds.add(vo.getExamId());
        }
        //批量插入
        redisService.multiSet(examMap);
        //先要对list进行删除操作
        redisService.deleteObject(getExamListKey(type));
        //插入list 尾插法
        redisService.rightPushAll(getExamListKey(type),examIds);
    }

    public String getExamListKey(Integer type) {
        //判断是查未完赛 还是 历史竞赛 来进行查询
        if(ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(type)) {
            return CacheConstants.EXAM_UNFINISHED_LIST_KEY;
        } else if(ExamListType.EXAM_HISTORY_LIST.getValue().equals(type)) {
            return CacheConstants.EXAM_HISTORY_LIST_KEY;
        }
        return null;
    }

    //获取redis中的exam数据对应的key
    public String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL_KEY_PREFIX+examId;
    }
}
