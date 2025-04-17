package com.example.friend.manager;

import cn.hutool.core.collection.CollUtil;
import com.example.common.redis.service.RedisService;
import com.example.core.constants.CacheConstants;
import com.example.core.domain.PageResult;
import com.example.core.enums.ExamListType;
import com.example.friend.domain.dto.ExamQueryDTO;
import com.example.friend.domain.vo.ExamQueryVO;
import com.example.friend.mapper.ExamMapper;
import com.example.friend.mapper.UserExamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExamCacheManager {

    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserExamMapper userExamMapper;

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

    public List<ExamQueryVO> assembleExamVOList(List<Long> examIds) {
        //根据examIds每一项获取到对应的缓存结构
        List<String> examDetailKeys = examIds.stream().map((examId) -> CacheConstants.EXAM_DETAIL_KEY_PREFIX + examId).toList();
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
}
