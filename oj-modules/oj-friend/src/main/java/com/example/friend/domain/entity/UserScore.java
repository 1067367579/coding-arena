package com.example.friend.domain.entity;

import lombok.Data;

@Data
public class UserScore {
    private Long examId;
    private Long userId;
    private Integer score;
    private Integer examRank;
}
