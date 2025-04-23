package com.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PassStatus {

    NOT_PASSED(0,"未通过"),
    PASSED(1,"通过"),
    NOT_SUBMIT(2,"请先提交代码"),
    IN_JUDGE(3,"正在判题中");

    private final int value;
    private final String description;

}
