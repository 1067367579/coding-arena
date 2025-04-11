package com.example.system.domain.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@TableName(value = "tb_exam")
public class Exam extends BaseEntity {
    @TableId(value = "exam_id", type = IdType.ASSIGN_ID)
    private Long examId;
    private String title;
    private String startTime;
    private String endTime;
    private Integer status;
}
