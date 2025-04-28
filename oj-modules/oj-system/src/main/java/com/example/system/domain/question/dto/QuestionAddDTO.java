package com.example.system.domain.question.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuestionAddDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
    @Min(value = 1,message = "难度数值只能在1到3之间")
    @Max(value = 3,message = "难度数值只能在1到3之间")
    private Integer difficulty;
    @Min(value = 1,message = "空间限制在1到最大值之间")
    @Max(value = Integer.MAX_VALUE,message = "空间限制在1到最大值之间")
    private Integer spaceLimit;
    @Min(value = 1,message = "时间限制在1到最大值之间")
    @Max(value = Integer.MAX_VALUE,message = "时间限制在1到最大值之间")
    private Integer timeLimit;
    @NotBlank(message = "测试用例不能为空")
    private String questionCase;
    @NotBlank(message = "默认代码不能为空")
    private String defaultCode;
    @NotBlank(message = "主函数代码不能为空")
    private String mainFunc;
}
