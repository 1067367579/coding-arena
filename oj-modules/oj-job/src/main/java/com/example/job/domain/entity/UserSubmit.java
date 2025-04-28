package com.example.job.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_user_submit")
public class UserSubmit extends BaseEntity {
    @TableId(value = "submit_id",type = IdType.ASSIGN_ID)
    private Long submitId;
    private Long userId;
    private Long questionId;
    private Long examId;
    private Integer programType;
    private String userCode;
    private Integer pass;
    private String exeMessage;
    private Integer score;
    private String caseJudgeRes;
}
