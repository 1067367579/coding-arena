package com.example.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.friend.domain.dto.ExamQueryDTO;
import com.example.friend.domain.entity.Exam;
import com.example.friend.domain.vo.ExamQueryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExamMapper extends BaseMapper<Exam> {

    List<ExamQueryVO> getExamList(ExamQueryDTO examQueryDTO);

}
