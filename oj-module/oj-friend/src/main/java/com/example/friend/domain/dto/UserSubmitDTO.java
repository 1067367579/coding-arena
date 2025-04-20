package com.example.friend.domain.dto;

import lombok.Data;

@Data
public class UserSubmitDTO {
    //此处不用userId ThreadLocal里面取出来
    private Long examId;
    private Long questionId;
    private Integer programType; //编程语言
    private String userCode; //用户编写的代码
}
