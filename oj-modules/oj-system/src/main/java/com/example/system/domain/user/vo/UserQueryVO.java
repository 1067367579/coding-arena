package com.example.system.domain.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class UserQueryVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String nickName;
    private Integer gender;
    private String phone;
    private String email;
    private String wechat;
    private String school;
    private String major;
    private String introduce;
    private Integer status;
}
