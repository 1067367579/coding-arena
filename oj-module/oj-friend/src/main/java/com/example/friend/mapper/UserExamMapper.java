package com.example.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.friend.domain.entity.UserExam;
import com.example.friend.domain.vo.ExamQueryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserExamMapper extends BaseMapper<UserExam> {
    List<ExamQueryVO> listMyExam(Long userId);
}
