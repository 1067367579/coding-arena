package com.example.system.domain.exam.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;
import java.util.TreeSet;

@Data
public class ExamQuestionDTO {
    @NotNull(message = "竞赛ID不能为空")
    private Long examId;
    private TreeSet<Long> questionIds;
}
