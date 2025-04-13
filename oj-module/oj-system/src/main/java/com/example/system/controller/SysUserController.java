package com.example.system.controller;

import com.example.core.constants.HttpConstants;
import com.example.core.controller.BaseController;
import com.example.core.domain.Result;
import com.example.system.domain.admin.dto.LoginDTO;
import com.example.system.domain.admin.dto.SysUserDTO;
import com.example.system.domain.admin.vo.LoginUserVO;
import com.example.system.domain.admin.vo.SysUserVO;
import com.example.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Tag(name = "管理员接口") //描述接口组别
@RequestMapping("/sys/user")
public class SysUserController extends BaseController {

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login") //为了传输时数据的安全性 使用POST请求方式
    @Operation(summary = "管理员用户登录",description = "根据账号密码登录管理员")
    @ApiResponse(responseCode = "1000",description = "操作成功")
    @ApiResponse(responseCode = "3002",description = "参数校验失败")
    @ApiResponse(responseCode = "3103",description = "用户名或密码错误")
    @ApiResponse(responseCode = "3102",description = "用户不存在")
    @ApiResponse(responseCode = "3103",description = "用户名或密码错误")
    @ApiResponse(responseCode = "3104",description = "被拉入黑名单")
    public Result<String> login(@RequestBody @Validated LoginDTO loginDTO) {
        log.info("管理员用户登录：{}",loginDTO);
        return sysUserService.login(loginDTO);
    }

    @PostMapping("/add")
    @Operation(summary = "新增管理员",description = "根据提供的信息新增管理员")
    @ApiResponse(responseCode = "1000",description = "操作成功")
    @ApiResponse(responseCode = "2000",description = "服务繁忙")
    @ApiResponse(responseCode = "3002",description = "参数校验失败")
    @ApiResponse(responseCode = "3101",description = "用户已存在")
    public Result<?> addUser(@RequestBody @Validated SysUserDTO userDTO) {
        log.info("新增管理员用户:{}",userDTO);
        int result = sysUserService.add(userDTO);
        return responseByService(result);
    }

    @DeleteMapping("/delete/{userId}")
    @Operation(summary = "删除用户",description = "通过用户ID删除用户")
    @Parameters(value = {
            @Parameter(name = "userId",in = ParameterIn.PATH,description = "用户ID")
    })
    @ApiResponse(responseCode = "1000", description = "成功删除⽤户")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3001", description = "未授权")
    @ApiResponse(responseCode = "3101", description = "⽤户不存在")
    public Result<?> deleteUser(@PathVariable Long userId) {
        return null;
    }

    @PutMapping("/update")
    @Operation(summary = "修改管理员用户",description = "根据提供的信息修改管理员信息")
    @ApiResponse(responseCode = "1000",description = "操作成功")
    @ApiResponse(responseCode = "2000",description = "服务繁忙")
    @ApiResponse(responseCode = "3002",description = "参数校验失败")
    public Result<?> updateUser(@RequestBody SysUserDTO userDTO) {
        return null;
    }

    @GetMapping("/detail")
    @Operation(summary = "获取用户详细信息",description = "根据用户ID获取用户详细信息")
    @Parameters(value = {
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "⽤户ID")
    })
    @ApiResponse(responseCode = "1000", description = "成功获取⽤户信息")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3101", description = "⽤户不存在")
    public Result<SysUserVO> detail(@RequestParam Long userId) {
        return null;
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户简单信息",description = "根据token获取用户简单信息")
    @Parameters(value = {
            @Parameter(name = "token",in = ParameterIn.HEADER,description = "令牌")
    })
    @ApiResponse(responseCode = "1000", description = "成功获取⽤户信息")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3101", description = "⽤户不存在")
    public Result<LoginUserVO> info(@RequestHeader(HttpConstants.AUTHENTICATION) String token) {
        log.info("获取到当前用户令牌为：{}",token);
        token = processToken(token);
        return sysUserService.info(token);
    }

    private static String processToken(String token) {
        return token.replaceFirst(HttpConstants.AUTHENTICATION_PREFIX,"");
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
        return responseByService(sysUserService.logout(token));
    }
}
