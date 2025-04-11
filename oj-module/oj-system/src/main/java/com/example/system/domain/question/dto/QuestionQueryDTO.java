package com.example.system.domain.question.dto;

import com.example.core.domain.PageQueryDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionQueryDTO extends PageQueryDTO {
    //题目标题
    private String title;
    //难度
    private Integer difficulty;
}
