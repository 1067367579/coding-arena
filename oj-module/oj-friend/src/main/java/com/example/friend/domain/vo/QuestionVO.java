package com.example.friend.domain.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuestionVO extends QuestionQueryVO{
    private Integer timeLimit;
    private Integer spaceLimit;
    private String content;
    private String defaultCode;

}
