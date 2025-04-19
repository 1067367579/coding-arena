package com.example.job.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.example.common.core.domain.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Exam extends BaseEntity {
    @TableId(value = "exam_id", type = IdType.ASSIGN_ID)
    private Long examId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
