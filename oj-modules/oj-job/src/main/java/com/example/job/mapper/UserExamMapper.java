package com.example.job.mapper;

import com.example.job.domain.entity.UserScore;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserExamMapper {

    void updateScoreAndRank(List<UserScore> userScoreList);
}
