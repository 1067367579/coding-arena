package com.example.system.domain.question.dto;

import com.example.core.domain.PageQueryDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class QuestionQueryDTO extends PageQueryDTO {
    //题目标题
    private String title;
    //难度
    private Integer difficulty;

    //前端传过来的集合字符串
    private String excludeIdSetStr;

    //已经选择的题目ID集合 选择Set集合避免重复
    private Set<Long> excludeIdSet;
}
