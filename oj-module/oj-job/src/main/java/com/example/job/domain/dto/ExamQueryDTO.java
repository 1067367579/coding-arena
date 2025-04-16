package com.example.job.domain.dto;

import com.example.core.domain.PageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ExamQueryDTO extends PageQueryDTO {

    private String title;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    //是获取未完赛还是历史竞赛
    @Min(value = 0,message = "获取类型只能是0或者1")
    @Max(value = 1,message = "获取类型只能是0或者1")
    private Integer type;

}
