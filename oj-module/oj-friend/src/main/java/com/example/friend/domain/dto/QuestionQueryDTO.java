package com.example.friend.domain.dto;

import com.example.common.core.domain.PageQueryDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuestionQueryDTO extends PageQueryDTO {
    //题目标题和内容关键字 二者其中一个满足即可
    private String keyword;
    //难度
    private Integer difficulty;
}
