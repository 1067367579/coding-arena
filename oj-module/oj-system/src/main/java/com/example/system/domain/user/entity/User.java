package com.example.system.domain.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.domain.BaseEntity;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("tb_user")
public class User extends BaseEntity {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;
    private String avatar;
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
