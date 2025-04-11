package com.example.system.domain.question.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionQueryVO {
    private Long questionId;
    private String title;
    private Integer difficulty;
    private String createName;
    private LocalDateTime createTime;
}
