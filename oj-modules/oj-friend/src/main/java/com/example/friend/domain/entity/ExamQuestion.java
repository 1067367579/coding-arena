package com.example.friend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(value = "tb_exam_question")
public class ExamQuestion extends BaseEntity {
    @TableId(value = "exam_question_id", type = IdType.ASSIGN_ID)
    private Long examQuestionId;
    private Long examId;
    private Long questionId;
    private Integer questionOrder;
}
