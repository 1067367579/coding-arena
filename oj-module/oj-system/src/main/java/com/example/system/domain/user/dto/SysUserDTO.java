package com.example.system.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "管理员用户DTO")
public class SysUserDTO {

    @NotBlank(message = "用户账号不能为空")
    @Schema(description = "用户账号")
    private String userAccount;

    @NotBlank(message = "用户密码不能为空")
    @Size(min = 5,max = 20,message = "密码长度不能少于6位，不能大于20位")
    @Schema(description = "用户密码")
    private String password;
}
