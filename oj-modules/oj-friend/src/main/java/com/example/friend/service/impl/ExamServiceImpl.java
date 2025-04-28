package com.example.friend.service.impl;

import com.example.common.core.constants.JwtConstants;
import com.example.common.core.domain.PageResult;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.common.security.exception.ServiceException;
import com.example.friend.domain.dto.ExamQueryDTO;
import com.example.friend.domain.dto.ExamRankDTO;
import com.example.friend.domain.vo.ExamQueryVO;
import com.example.friend.domain.vo.ExamRankVO;
import com.example.friend.domain.vo.UserVO;
import com.example.friend.manager.ExamCacheManager;
import com.example.friend.manager.ExamRankCacheManager;
import com.example.friend.manager.UserCacheManager;
import com.example.friend.mapper.ExamMapper;
import com.example.friend.mapper.UserExamMapper;
import com.example.friend.rabbit.CacheRefreshProducer;
import com.example.friend.service.ExamService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamCacheManager examCacheManager;

    @Autowired
    private ExamRankCacheManager examRankCacheManager;

    @Autowired
    private UserExamMapper userExamMapper;

    @Autowired
    private CacheRefreshProducer cacheRefreshProducer;

    @Autowired
    private UserCacheManager userCacheManager;

    @Override
    public List<ExamQueryVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.getExamList(examQueryDTO);
    }

    @Override
    public PageResult redisList(ExamQueryDTO examQueryDTO) {
        //C端对redis进行操作 从redis中获取竞赛列表的数据
        //两级缓存 第一级列表存储id 第二级存储对象
        Long listSize = examCacheManager.getListSize(examQueryDTO.getType(),null);
        if(listSize == null || listSize == 0) {
            //没有数据的时候 去数据库里面刷新数据
            examCacheManager.refreshCache(examQueryDTO.getType(),null);
        }
        PageResult examVOList = examCacheManager.getExamVOList(examQueryDTO, null);
        Object userIdObject = ThreadLocalUtil.getLocalMap().get(JwtConstants.USER_ID);
        if(userIdObject == null) {
            return examVOList;
        }
        long userId = (Long) userIdObject;
        List<Long> enterExamIds = examCacheManager.getEnterExamList(userId);
        List<ExamQueryVO> list = examVOList.getRows().stream().map(
                exam -> {
                    ExamQueryVO examQueryVO = (ExamQueryVO) exam;
                    Long examId = examQueryVO.getExamId();
                    examQueryVO.setEnter(enterExamIds.contains(examId));
                    return examQueryVO;
                }
        ).toList();
        examVOList.setRows(list);
        return examVOList;
    }

    @Override
    public String getFirstQuestion(Long examId) {
        checkExamTime(examId);
        checkExamQuestionListCache(examId);
        //还是一样先查redis 查不到查MySQL MySQL都查不到报错
        Long firstQuestion = examCacheManager.getFirstQuestion(examId);
        return String.valueOf(firstQuestion);
    }

    private void checkExamQuestionListCache(Long examId) {
        //获取长度
        Long size = examCacheManager.getExamQuestionListSize(examId);
        if(size == 0) {
            //如果长度为0 说明没有数据 刷新
            examCacheManager.refreshExamQuestionList(examId);
        }
    }

    private void checkExamTime(Long examId) {
        //只有开赛了才能调用这个接口 不然会有泄题风险 之前的redis缓存有竞赛数据
        ExamQueryVO examQueryVO = examCacheManager.getExamQueryVO(examId);
        //判断开始时间
        if(examQueryVO.getStartTime().isAfter(LocalDateTime.now())) {
            //还未开赛 不能获取
            throw new ServiceException(ResultCode.FAILED_EXAM_NOT_START);
        }
    }

    public String preQuestion(Long examId, Long questionId) {
        checkExamTime(examId);
        checkExamQuestionListCache(examId);
        return examCacheManager.preQuestion(examId,questionId).toString();
    }

    public String nextQuestion(Long examId, Long questionId) {
        checkExamTime(examId);
        checkExamQuestionListCache(examId);
        return examCacheManager.nextQuestion(examId,questionId).toString();
    }

    @Override
    public PageResult rankList(ExamRankDTO examRankDTO) {
        Long examId = examRankDTO.getExamId();
        //先查缓存有无
        Long size = examRankCacheManager.getListSize(examId);
        if(size == null || size == 0) {
            //没有缓存，从数据库中查，同时异步刷新
            PageHelper.startPage(examRankDTO.getPageNum(), examRankDTO.getPageSize());
            List<ExamRankVO> examRankList = userExamMapper.getExamRankList(examId);
            if(CollectionUtils.isEmpty(examRankList)) {
                return PageResult.empty();
            }
            //查到之后 发送异步消息刷新缓存
            cacheRefreshProducer.produceRankRefresh(examId);
            //转化为VO
            assembleVOList(examRankList);
            return getPageResult(examRankList);
        }
        //有缓存 直接拿
        List<ExamRankVO> examRankList= examRankCacheManager.getVOList(examRankDTO);
        assembleVOList(examRankList);
        return PageResult.success(examRankList,size);
    }

    public void assembleVOList(List<ExamRankVO> examRankVOList) {
        //每个去用户缓存里面查
        for (ExamRankVO examRankVO : examRankVOList) {
            Long userId = examRankVO.getUserId();
            UserVO userVO = userCacheManager.getUserDetail(userId);
            examRankVO.setNickName(userVO.getNickName());
        }
    }

    //分页查询之后 统一获取到查询到信息的总个数
    public PageResult getPageResult(List<?> rows) {
        long total = new PageInfo<>(rows).getTotal();
        return PageResult.success(rows,total);
    }
}
