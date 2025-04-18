package com.example.friend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.core.domain.PageResult;
import com.example.friend.domain.dto.QuestionQueryDTO;
import com.example.friend.domain.entity.Question;
import com.example.friend.domain.entity.QuestionES;
import com.example.friend.domain.vo.QuestionQueryVO;
import com.example.friend.elasticsearch.QuestionRepository;
import com.example.friend.mapper.QuestionMapper;
import com.example.friend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public PageResult list(QuestionQueryDTO questionQueryDTO) {
        //先看es有无数据 若没有再去mysql中拿
        long count = questionRepository.count();
        if(count == 0) {
            refreshQuestion();
        }
        //现在ES于MySQL一致 判断DTO的条件情况是怎么样 判断
        //keyword可能是content或者是title 交给ES自行判断 两个条件都按照keyword搜
        String keyword = questionQueryDTO.getKeyword();
        Integer difficulty = questionQueryDTO.getDifficulty();
        //排序规则对象
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        //分页查询对象 还要在此设置排序规则
        //这里PageRequest对象的页码是以0开始的
        PageRequest pageRequest = PageRequest.of(questionQueryDTO.getPageNum()-1,
                questionQueryDTO.getPageSize(),sort);
        Page<QuestionES> page;
        //判断
        if(difficulty == null && !StringUtils.hasLength(keyword)) {
            //没有条件 查询全部
            page = questionRepository.findAll(pageRequest);
        } else if(!StringUtils.hasLength(keyword)) {
            page = questionRepository.findByDifficulty(difficulty,pageRequest);
        } else if(difficulty == null) {
            page = questionRepository.findByTitleOrContent(keyword,keyword,pageRequest);
        } else {
            page = questionRepository.findByTitleOrContentAndDifficulty(keyword,keyword,difficulty,pageRequest);
        }
        //获取到了Page 然后进行处理返回给前端
        //获取总数
        long totalElements = page.getTotalElements();
        List<QuestionES> content = page.getContent();
        //转换VO
        List<QuestionQueryVO> contentVO = BeanUtil.copyToList(content, QuestionQueryVO.class);
        return PageResult.success(contentVO,totalElements);
    }

    public void refreshQuestion() {
        //没有数据 刷新
        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<Question>());
        if(CollectionUtils.isEmpty(questions)) {
            return;
        }
        List<QuestionES> questionESList = BeanUtil.copyToList(questions, QuestionES.class);
        //ES 刷新
        questionRepository.saveAll(questionESList);
    }
}
