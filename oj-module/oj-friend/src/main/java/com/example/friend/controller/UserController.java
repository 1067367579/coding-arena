package com.example.friend.controller;

import com.example.common.core.constants.HttpConstants;
import com.example.common.core.controller.BaseController;
import com.example.common.core.domain.LoginUser;
import com.example.common.core.domain.Result;
import com.example.friend.domain.dto.SendCodeDTO;
import com.example.friend.domain.dto.UserEditDTO;
import com.example.friend.domain.dto.UserLoginDTO;
import com.example.friend.domain.vo.UserVO;
import com.example.friend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @PostMapping("/code")
    public Result<?> sendCode(@RequestBody @Validated SendCodeDTO sendCodeDTO) {
        log.info("根据邮箱号发送短信验证码:{}", sendCodeDTO);
        return userService.sendCode(sendCodeDTO);
    }


    @PostMapping("/login")
    public Result<?> login(@RequestBody @Validated UserLoginDTO loginDTO) {
        log.info("用户登录：{}",loginDTO);
        return Result.ok(userService.login(loginDTO));
    }


    @DeleteMapping("/logout")
    @Operation(summary = "退出登录",description = "用户根据token退出登录")
    @Parameters(value = {
            @Parameter(name = "token",in = ParameterIn.HEADER,description = "令牌")
    })
    @ApiResponse(responseCode = "1000", description = "成功退出登录")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3001", description = "未授权")
    @ApiResponse(responseCode = "3101", description = "⽤户不存在")
    public Result<?> logout(@RequestHeader(HttpConstants.AUTHENTICATION) String token) {
        log.info("获取到当前用户令牌为：{}",token);
        token = processToken(token);
        return responseByService(userService.logout(token));
    }

    @GetMapping("/info")
    public Result<LoginUser> getUserInfo(@RequestHeader(HttpConstants.AUTHENTICATION) String token) {
        log.info("获取到当前用户令牌为：{}",token);
        token= processToken(token);
        return userService.info(token);
    }

    @GetMapping("/detail")
    public Result<UserVO> detail() {
        log.info("查看用户详细信息");
        return Result.ok(userService.detail());
    }

    @PutMapping("/edit")
    public Result<?> updateUser(@RequestBody UserEditDTO userEditDTO) {
        log.info("修改用户信息:{}",userEditDTO);
        return responseByService(userService.edit(userEditDTO));
    }

    @PostMapping("/avatar/update")
    public Result<?> updateAvatar(@RequestBody UserEditDTO userEditDTO) {
        log.info("修改用户头像:{}",userEditDTO);
        return responseByService(userService.updateAvatar(userEditDTO));
    }
}
