package com.example.friend.domain.vo;

import lombok.Data;

@Data
public class QuestionQueryVO {
    private Long questionId;
    private String title;
    private Integer difficulty;
}
