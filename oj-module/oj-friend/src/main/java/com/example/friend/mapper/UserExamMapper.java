package com.example.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.friend.domain.entity.UserExam;
import com.example.friend.domain.vo.ExamQueryVO;
import com.example.friend.domain.vo.ExamRankVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserExamMapper extends BaseMapper<UserExam> {
    List<ExamQueryVO> listMyExam(Long userId);

    @Select("select user_id,score,exam_rank from tb_user_exam where exam_id = #{examId} " +
            "and score is not null and exam_rank is not null" +
            " order by exam_rank")
    List<ExamRankVO> getExamRankList(Long examId);
}
