package com.example.common.core.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//Redis存储使用
public class LoginUser {
    //用户身份 1 普通用户 2 管理员
    private Integer identity;
    //昵称
    private String nickName;
    //用户头像 C端使用
    private String avatar;
}
