package com.example.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.system.domain.exam.dto.ExamQueryDTO;
import com.example.system.domain.exam.entity.Exam;
import com.example.system.domain.exam.vo.ExamQueryVO;
import jakarta.validation.constraints.Max;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExamMapper extends BaseMapper<Exam> {

    List<ExamQueryVO> getExamList(ExamQueryDTO examQueryDTO);
}
