package com.example.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "管理员用户登录DTO")
public class LoginDTO {
    @Schema(description = "用户帐号")
    private String userAccount;
    @Schema(description = "用户密码")
    private String password;
}
