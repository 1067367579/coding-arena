package com.example.system.domain.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@TableName("tb_question")
@Getter
@Setter
public class Question extends BaseEntity {
    @TableId(value = "question_id", type = IdType.ASSIGN_ID)
    private Long questionId;
    private String title;
    private Integer difficulty;
    private Integer timeLimit;
    private Integer spaceLimit;
    private String content;
    private String questionCase;
    private String defaultCode;
    private String mainFunc;
}
