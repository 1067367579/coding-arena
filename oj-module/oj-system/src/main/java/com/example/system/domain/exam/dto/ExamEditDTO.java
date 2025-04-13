package com.example.system.domain.exam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExamEditDTO extends ExamAddDTO{
    private Long examId;
}
