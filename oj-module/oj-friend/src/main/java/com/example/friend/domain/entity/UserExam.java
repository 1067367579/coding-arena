package com.example.friend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("tb_user_exam")
public class UserExam extends BaseEntity {
    @TableId(value = "user_exam_id",type = IdType.ASSIGN_ID)
    private Long userExamId;
    private Long examId;
    private Long userId;
    private Integer score;
    private Integer examRank;
}
