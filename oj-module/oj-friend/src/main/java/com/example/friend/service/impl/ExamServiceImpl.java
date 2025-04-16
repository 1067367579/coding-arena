package com.example.friend.service.impl;

import com.example.core.domain.PageResult;
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
        //新发布的竞赛会放到缓存中 但是还有老数据 还是应该存到redis中
        //查询的时候做处理 redis中查不到 到数据库中查出 刷新缓存 返回
        //之后再查就可以从redis里面查得到了 针对性刷新
        Long listSize = examCacheManager.getListSize(examQueryDTO.getType());
        if(listSize == null || listSize == 0) {
            //没有数据的时候 去数据库里面刷新数据
            examCacheManager.refreshCache(examQueryDTO.getType());
        }
        return examCacheManager.getExamVOList(examQueryDTO);
    }
}
