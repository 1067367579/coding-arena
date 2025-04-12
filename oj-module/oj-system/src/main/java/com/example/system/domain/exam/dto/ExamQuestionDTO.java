package com.example.system.domain.exam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.LinkedHashSet;

@Data
public class ExamQuestionDTO {
    @NotNull(message = "竞赛ID不能为空")
    private Long examId;
    private LinkedHashSet<Long> questionIds;
}
