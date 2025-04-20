package com.example.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public enum ProgramType {

    JAVA(0,"Java"),
    CPP(1,"CPP");

    private final Integer value;
    private final String description;
}
