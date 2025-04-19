package com.example.friend.service.impl;

import com.example.common.core.constants.JwtConstants;
import com.example.common.core.domain.PageResult;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.friend.domain.dto.ExamQueryDTO;
import com.example.friend.domain.vo.ExamQueryVO;
import com.example.friend.manager.ExamCacheManager;
import com.example.friend.mapper.ExamMapper;
import com.example.friend.service.ExamService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamCacheManager examCacheManager;

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
}
