package com.example.judge.domain.result;

import lombok.Data;

@Data
public class CompileResult {
    private boolean compiled; //编译是否成功
    private String exeMessage; //执行的信息
}
