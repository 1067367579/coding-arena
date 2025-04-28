package com.example.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.job.domain.entity.UserScore;
import com.example.job.domain.entity.UserSubmit;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserSubmitMapper extends BaseMapper<UserSubmit> {


    List<UserScore> getUserScoreList(List<Long> examIds);

}
