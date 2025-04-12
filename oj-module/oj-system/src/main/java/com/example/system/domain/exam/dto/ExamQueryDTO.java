package com.example.system.domain.exam.dto;

import com.example.core.domain.PageQueryDTO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ExamQueryDTO extends PageQueryDTO {
    private String startTime;
    private String endTime;
    private String title;
    private Integer status;
}
