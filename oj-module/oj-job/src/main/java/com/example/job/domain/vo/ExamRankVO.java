package com.example.job.domain.vo;

import lombok.Data;

@Data
public class ExamRankVO {
    private Long userId;
    private Integer score;
    private Integer examRank;
    private String nickName;
}
