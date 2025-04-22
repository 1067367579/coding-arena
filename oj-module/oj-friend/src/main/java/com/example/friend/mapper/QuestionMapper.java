package com.example.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.friend.domain.entity.Question;
import com.example.friend.domain.vo.QuestionQueryVO;
import com.example.friend.domain.vo.QuestionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    @Select("select question_id, title, difficulty from tb_question where question_id = #{questionId}")
    QuestionQueryVO getVOById(Long questionId);
}
