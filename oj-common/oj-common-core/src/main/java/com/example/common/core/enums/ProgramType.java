package com.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProgramType {

    JAVA(0,"Java"),
    CPP(1,"CPP");

    private final Integer value;
    private final String description;
}
