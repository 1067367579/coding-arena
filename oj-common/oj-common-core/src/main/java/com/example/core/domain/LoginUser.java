package com.example.core.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUser {
    //用户身份 1 普通用户 2 管理员
    private Integer identity;
}
