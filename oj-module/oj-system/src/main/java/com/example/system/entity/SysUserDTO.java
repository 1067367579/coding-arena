package com.example.system.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "管理员用户DTO")
public class SysUserDTO {
    @Schema(description = "用户账号")
    private String userAccount;
    @Schema(description = "用户密码")
    private String password;
}
