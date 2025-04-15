package com.example.friend.manager;

import cn.hutool.core.collection.CollUtil;
import com.example.common.redis.service.RedisService;
import com.example.core.constants.CacheConstants;
import com.example.friend.domain.dto.ExamQueryDTO;
import com.example.friend.domain.enums.ExamListType;
import com.example.friend.domain.vo.ExamQueryVO;
import com.example.friend.mapper.ExamMapper;
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

    public List<ExamQueryVO> getExamVOList(ExamQueryDTO examQueryDTO) {
        //手动处理分页参数 得到查询范围
        int start = (examQueryDTO.getPageNum()-1) * examQueryDTO.getPageSize();
        int end = start + examQueryDTO.getPageSize() - 1;
        String examListKey = getExamListKey(examQueryDTO.getType());
        List<Long> examIds = redisService.getCacheListByRange(examListKey,start,end,Long.class);
        List<ExamQueryVO> voList = assembleExamVOList(examIds);
        if(CollectionUtils.isEmpty(examIds) || CollectionUtils.isEmpty(voList)){
            //列表中该段序列没有缓存 需要重新刷新缓存 从数据库中获取全量数据后 再从缓存中获取
            refreshCache(examQueryDTO.getType());
            examIds = redisService.getCacheListByRange(examListKey,start,end,Long.class);
            voList = assembleExamVOList(examIds);
            return voList;
        }
        //已经有缓存 直接返回即可
        return voList;
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

    public Long getListSize(Integer type) {
        return redisService.getListSize(getExamListKey(type));
    }

    //将数据从数据库中刷新到缓存 该操作不会根据DTO筛选
    public void refreshCache(Integer type) {
        //刷新指定类型的list结构
        //直接从数据库中先获取出对应的数据
        ExamQueryDTO examQueryDTO = new ExamQueryDTO();
        examQueryDTO.setType(type);
        List<ExamQueryVO> examList = examMapper.getExamList(examQueryDTO);
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
