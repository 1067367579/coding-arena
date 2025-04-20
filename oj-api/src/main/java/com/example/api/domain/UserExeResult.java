package com.example.api.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserExeResult {
    private String input;
    private String output; //理论输出
    private String exeOutput; //实际输出
}
