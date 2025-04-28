package com.example.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.friend.domain.QuestionSubmit;
import com.example.friend.domain.entity.UserSubmit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserSubmitMapper extends BaseMapper<UserSubmit> {

    @Select("select question_id,count(*) as count from tb_user_submit group by question_id order by count(*) desc")
    List<QuestionSubmit> getQuestionSubmitStatistics();
}
