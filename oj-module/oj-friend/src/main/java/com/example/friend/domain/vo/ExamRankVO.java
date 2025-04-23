package com.example.friend.domain.vo;

import lombok.Data;
import org.springframework.boot.convert.DataSizeUnit;

@Data
public class ExamRankVO {
    private Long userId;
    private Integer score;
    private Integer examRank;
    private String nickName;
}
