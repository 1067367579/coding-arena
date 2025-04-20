package com.example.judge.domain;

import com.example.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserSubmit extends BaseEntity {
    private Long submitId;
    private Long userId;
    private Long questionId;
    private Long examId;
    private Integer programType;
    private String userCode;
    private Integer pass;
    private String exeMessage;
    private Integer score;
}
