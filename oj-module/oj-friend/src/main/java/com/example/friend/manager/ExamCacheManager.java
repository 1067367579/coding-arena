package com.example.friend.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.domain.PageResult;
import com.example.common.core.enums.ExamListType;
import com.example.common.core.enums.ResultCode;
import com.example.common.redis.service.RedisService;
import com.example.common.security.exception.ServiceException;
import com.example.friend.domain.dto.ExamQueryDTO;
import com.example.friend.domain.entity.Exam;
import com.example.friend.domain.entity.ExamQuestion;
import com.example.friend.domain.vo.ExamQueryVO;
import com.example.friend.mapper.ExamMapper;
import com.example.friend.mapper.ExamQuestionMapper;
import com.example.friend.mapper.UserExamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class ExamCacheManager {

    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserExamMapper userExamMapper;
    @Autowired
    private ExamQuestionMapper examQuestionMapper;

    public PageResult getExamVOList(ExamQueryDTO examQueryDTO,Long userId) {
        //手动处理分页参数 得到最终的结果范围
        int start = (examQueryDTO.getPageNum()-1) * examQueryDTO.getPageSize();
        int end = start + examQueryDTO.getPageSize()-1;
        //获得List的key
        String examListKey = getExamListKey(examQueryDTO.getType(),userId);
        //全量长度
        Long listSize = getListSize(examQueryDTO.getType(),userId);
        //分开处理有时间筛选和没时间筛选的情况
        List<Long> examIds;
        List<ExamQueryVO> voList;
        if(examQueryDTO.getStartTime() == null &&
        examQueryDTO.getEndTime() == null){
            //redis获取区间的操作 左闭右闭 没有时间筛选查询 直接分页 开始时间筛选和结束时间必须一起存在
            examIds = redisService.getCacheListByRange(examListKey,start,end,Long.class);
            voList = assembleExamVOList(examIds);
            if(CollectionUtils.isEmpty(examIds) || CollectionUtils.isEmpty(voList)){
                //列表中该段序列没有缓存 需要重新刷新缓存 从数据库中获取全量数据后 再从缓存中获取
                refreshCache(examQueryDTO.getType(),userId);
                examIds = redisService.getCacheListByRange(examListKey,start,end,Long.class);
                voList = assembleExamVOList(examIds);
                return PageResult.success(voList,listSize);
            }
            return PageResult.success(voList,listSize);
        } else {
            //有时间筛选查询 先查出来全量 再进行分页
            examIds = redisService.getCacheListByRange(examListKey,0,listSize,Long.class);
            voList = assembleExamVOList(examIds);
            voList = voList.stream().filter(examQueryVO -> {
                boolean startTime = true;
                boolean endTime = true;
                if(examQueryDTO.getStartTime()!=null) {
                    startTime = examQueryVO.getStartTime().isAfter(examQueryDTO.getStartTime())
                    || examQueryVO.getStartTime().equals(examQueryDTO.getStartTime());
                }
                if(examQueryDTO.getEndTime()!=null) {
                    endTime = examQueryVO.getEndTime().isBefore(examQueryDTO.getEndTime())
                    || examQueryVO.getEndTime().equals(examQueryDTO.getEndTime());
                }
                return startTime && endTime;
            }).toList();
            long resTotal = voList.size();
            //分页参数设置
            end = end + 1;
            if(start>=voList.size()){
                return PageResult.success(List.of(),resTotal);
            }
            if(end>voList.size()){
                end = voList.size();
            }
            //分页 左闭右开
            voList = voList.subList(start,end);
            //返回结果
            return PageResult.success(voList,resTotal);
        }
    }

    //拿到竞赛详细信息的key获取出详细信息
    public ExamQueryVO getExamQueryVO(Long examId) {
        String examDetailKey = getDetailKey(examId);
        ExamQueryVO examQueryVO = redisService.getCacheObject(examDetailKey, ExamQueryVO.class);
        if(examQueryVO == null) {
            //去mysql中找 然后刷新vo
            Exam exam = examMapper.selectById(examId);
            if(exam == null) {
                throw new ServiceException(ResultCode.FAILED_EXAM_NOT_EXISTS);
            }
            examQueryVO = BeanUtil.toBean(exam, ExamQueryVO.class);
            redisService.setCacheObject(examDetailKey, examQueryVO);
        }
        //拿到examVO 返回
        return examQueryVO;
    }

    public List<ExamQueryVO> assembleExamVOList(List<Long> examIds) {
        //根据examIds每一项获取到对应的缓存结构
        List<String> examDetailKeys = examIds.stream().map(this::getDetailKey).toList();
        List<ExamQueryVO> voList = redisService.multiGet(examDetailKeys, ExamQueryVO.class);
        CollUtil.removeNull(voList);
        //有错误 返回 空白列表
        if(CollectionUtils.isEmpty(voList) || examIds.size() != voList.size()){
            return List.of();
        }
        return voList;
    }

    public Long getListSize(Integer type,Long userId) {
        return redisService.getListSize(getExamListKey(type,userId));
    }

    //将数据从数据库中刷新到缓存 该操作不会根据DTO筛选
    public void refreshCache(Integer type,Long userId) {
        //刷新指定类型的list结构
        //直接从数据库中先获取出对应的数据
        ExamQueryDTO examQueryDTO = new ExamQueryDTO();
        examQueryDTO.setType(type);
        List<ExamQueryVO> examList;
        if(userId == null){
            examList = examMapper.getExamList(examQueryDTO);
        } else {
            examList = userExamMapper.listMyExam(userId);
        }
        //如果数据库中没有数据 直接返回 无需后续操作
        if(CollectionUtils.isEmpty(examList)) {
            return;
        }
        //有数据 创建一个HashMap 批量插入exam examId到list结构中
        Map<String,ExamQueryVO> examMap = new HashMap<>();
        List<Long> examIds = new ArrayList<>();
        //遍历返回数据
        for (ExamQueryVO vo : examList) {
            examMap.put(getDetailKey(vo.getExamId()),vo);
            examIds.add(vo.getExamId());
        }
        //批量插入
        redisService.multiSet(examMap);
        //先要对list进行删除操作
        redisService.deleteObject(getExamListKey(type,userId));
        //插入list 尾插法 userExam而言 从数据库中获取到的顺序是正确的
        redisService.rightPushAll(getExamListKey(type,userId),examIds);
    }

    public void addUserExamCache(Long userId,Long examId) {
        //获取到key值存入即可 是根据用户来存examId的 用户竞赛列表缓存 后加入的在前面
        redisService.leftPushForList(getExamListKey(ExamListType.EXAM_MY_LIST.getValue(),userId),examId);
    }

    public String getExamListKey(Integer type,Long userId) {
        //判断是查未完赛 还是 历史竞赛 来进行查询
        if(ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(type)) {
            return CacheConstants.EXAM_UNFINISHED_LIST_KEY;
        } else if(ExamListType.EXAM_HISTORY_LIST.getValue().equals(type)) {
            return CacheConstants.EXAM_HISTORY_LIST_KEY;
        } else {
            return CacheConstants.USER_EXAM_LIST_KEY_PREFIX+userId;
        }
    }

    //获取redis中的exam数据对应的key
    public String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL_KEY_PREFIX+examId;
    }

    //从用户报名竞赛列表中取出所有的报名竞赛ID 通过这个来判断
    public List<Long> getEnterExamList(Long userId) {
        List<Long> enterExamIds = redisService.getCacheListByRange(
                getExamListKey(ExamListType.EXAM_MY_LIST.getValue(),userId),
                0,-1, Long.class
        );
        if(CollectionUtils.isEmpty(enterExamIds)) {
            refreshCache(ExamListType.EXAM_MY_LIST.getValue(),userId);
            return redisService.getCacheListByRange(
                    getExamListKey(ExamListType.EXAM_MY_LIST.getValue(),userId),
                    0,-1, Long.class
            );
        }
        return enterExamIds;
    }

    public Long getFirstQuestion(Long examId) {
        String examQuestionListKey = getExamQuestionListKey(examId);
        //刷新完redis之后 直接获取出来 获取第一个元素
        Object object = redisService.indexOf(examQuestionListKey, 0);
        if(object == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        return (Long) object;
    }



    public void refreshExamQuestionList(Long examId) {
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .select(ExamQuestion::getQuestionId)
                .eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getQuestionOrder)
        );
        if(CollectionUtils.isEmpty(examQuestions)) {
            return;
        }
        List<Long> questionIds = examQuestions.stream().map(ExamQuestion::getQuestionId)
                .toList();
        String examQuestionListKey = getExamQuestionListKey(examId);
        //存到Cache中 先删再存
        redisService.deleteObject(examQuestionListKey);
        redisService.rightPushAll(examQuestionListKey,questionIds);
        redisService.expire(examQuestionListKey, ChronoUnit.SECONDS.between(LocalDateTime.now(),
                LocalDateTime.now().plusDays(1).plusHours(0).plusMinutes(0).plusSeconds(0)
                        .plusNanos(0)
        ), TimeUnit.SECONDS);
    }

    public Long getExamQuestionListSize(Long examId) {
        String examQuestionListKey = getExamQuestionListKey(examId);
        return redisService.getListSize(examQuestionListKey);
    }

    public String getExamQuestionListKey(Long examId) {
        return CacheConstants.EXAM_QUESTION_LIST_KEY_PREFIX+examId;
    }

    public Long preQuestion(Long examId, Long questionId) {
        //刷新了缓存 获取
        Long size = getExamQuestionListSize(examId);
        if(size == 0) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        String examQuestionListKey = getExamQuestionListKey(examId);
        Long index = redisService.indexOfForList(examQuestionListKey, questionId);
        if(index == 0) {
            throw new ServiceException(ResultCode.FAILED_FIRST_QUESTION);
        }
        return (Long) redisService.indexOf(examQuestionListKey,index-1);
    }

    public Long nextQuestion(Long examId, Long questionId) {
        Long size = getExamQuestionListSize(examId);
        if(size == 0) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        String examQuestionListKey = getExamQuestionListKey(examId);
        Long index = redisService.indexOfForList(examQuestionListKey, questionId);
        if(index == size-1) {
            throw new ServiceException(ResultCode.FAILED_LAST_QUESTION);
        }
        return (Long) redisService.indexOf(examQuestionListKey,index+1);
    }
}
