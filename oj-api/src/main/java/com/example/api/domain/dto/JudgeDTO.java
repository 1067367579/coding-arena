package com.example.api.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JudgeDTO {
    private Long userId;
    private Long examId;
    private Long questionId;
    private Integer programType;
    private Integer difficulty;
    private Long timeLimit;
    private Long spaceLimit;
    private String userCode;
    //输入参数
    private List<String> inputList;
    //输出参数
    private List<String> outputList;
}
