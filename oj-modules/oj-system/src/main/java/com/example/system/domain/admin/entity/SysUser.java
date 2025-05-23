package com.example.system.domain.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;


@TableName("tb_sys_user")
@Getter
@Setter
public class SysUser extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;
    private String userAccount;
    private String password;
    private String nickName;

    @Override
    public String toString() {
        return "SysUser{" +
                "userId=" + userId +
                ", userAccount='" + userAccount + '\'' +
                ", password='" + password + '\'' +
                ", nickName='" + nickName + '\'' +
                "} " + super.toString();
    }
}
