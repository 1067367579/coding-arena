package com.example.friend.domain.dto;

import lombok.Data;

@Data
public class UserEditDTO {
    private String avatar;
    private String nickName;
    private Integer gender;
    private String school;
    private String major;
    private String phone;
    private String wechat;
    private String introduce;
}
