package com.example.friend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.constants.UserConstant;
import com.example.common.core.domain.BaseEntity;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

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

    //插入时生成一个默认的用户对象
    public static User defaultUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setGender(UserConstant.DEFAULT_GENDER);
        user.setNickName(UserConstant.DEFAULT_NAME_PREFIX+ UUID.randomUUID().toString().substring(0,6));
        user.setPhone(UserConstant.DEFAULT_PHONE);
        user.setAvatar(UserConstant.DEFAULT_USER_AVATAR);
        user.setSchool(UserConstant.DEFAULT_SCHOOL);
        user.setMajor(UserConstant.DEFAULT_MAJOR);
        user.setIntroduce(UserConstant.DEFAULT_INTRODUCE);
        user.setStatus(UserConstant.DEFAULT_STATUS);
        user.setWechat(UserConstant.DEFAULT_WECHAT);
        return user;
    }
}
