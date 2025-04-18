package com.example.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.friend.domain.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
