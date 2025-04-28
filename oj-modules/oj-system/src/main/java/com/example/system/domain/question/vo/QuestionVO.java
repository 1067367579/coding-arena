package com.example.system.domain.question.vo;

import lombok.Data;

@Data
public class QuestionVO {
    private String title;
    private String content;
    private Integer difficulty;
    private Integer spaceLimit;
    private Integer timeLimit;
    private String questionCase;
    private String defaultCode;
    private String mainFunc;
}
