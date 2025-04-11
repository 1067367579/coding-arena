package com.example.system.domain.exam.dto;

import com.example.core.domain.PageQueryDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamQueryDTO extends PageQueryDTO {
    private String startTime;
    private String endTime;
    private String title;
    private Integer status;
}
