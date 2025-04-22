package com.example.friend.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String nickName;
    private String avatar;
    private Integer gender;
    private String phone;
    private String email;
    private String wechat;
    private String school;
    private String major;
    private String introduce;
    private Integer status;
}
