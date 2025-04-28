package com.example.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.job.domain.dto.ExamQueryDTO;
import com.example.job.domain.entity.Exam;
import com.example.job.domain.vo.ExamQueryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExamMapper extends BaseMapper<Exam> {
    List<ExamQueryVO> getExamList(ExamQueryDTO examQueryDTO);
}
