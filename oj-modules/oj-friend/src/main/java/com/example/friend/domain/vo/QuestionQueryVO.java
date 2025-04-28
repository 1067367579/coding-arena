package com.example.friend.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class QuestionQueryVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long questionId;
    private String title;
    private Integer difficulty;
}
