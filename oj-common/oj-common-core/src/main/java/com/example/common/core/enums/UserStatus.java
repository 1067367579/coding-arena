package com.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserStatus {

    NORMAL(1,"正常"),
    FROZEN(0,"拉黑");

    private final Integer status;
    private final String description;
}
