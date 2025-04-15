package com.example.friend.domain.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SendCodeDTO {
    //封装为DTO 使用RequestBody加密传输 是因为邮箱号 手机号等都属于用户的敏感信息 需要加密传输
    @Pattern(regexp = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$"
            ,message = "邮箱号格式不正确")
    private String email;
}
