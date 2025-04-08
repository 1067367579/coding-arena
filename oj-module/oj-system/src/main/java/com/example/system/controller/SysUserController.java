package com.example.system.controller;

import com.example.core.domain.Result;
import com.example.system.entity.LoginDTO;
import com.example.system.entity.SysUserDTO;
import com.example.system.entity.SysUserVO;
import com.example.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Tag(name = "管理员接口") //描述接口组别
@RequestMapping("/sys/user")
public class SysUserController {

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
    public Result<String> login(@RequestBody LoginDTO loginDTO) {
        log.info("管理员用户登录：{}",loginDTO);
        return sysUserService.login(loginDTO);
    }

    @PostMapping("/add")
    @Operation(summary = "新增管理员",description = "根据提供的信息新增管理员")
    @ApiResponse(responseCode = "1000",description = "操作成功")
    @ApiResponse(responseCode = "2000",description = "服务繁忙")
    @ApiResponse(responseCode = "3002",description = "参数校验失败")
    @ApiResponse(responseCode = "3101",description = "用户已存在")
    public Result<Void> addUser(@RequestBody SysUserDTO userDTO) {
        log.info("新增管理员用户:{}",userDTO);
        return new Result<>();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户",description = "通过用户ID删除用户")
    @Parameters(value = {
            @Parameter(name = "userId",in = ParameterIn.PATH,description = "用户ID")
    })
    @ApiResponse(responseCode = "1000", description = "成功删除⽤户")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3001", description = "未授权")
    @ApiResponse(responseCode = "3101", description = "⽤户不存在")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        return null;
    }

    @PutMapping("/update")
    @Operation(summary = "修改管理员用户",description = "根据提供的信息修改管理员信息")
    @ApiResponse(responseCode = "1000",description = "操作成功")
    @ApiResponse(responseCode = "2000",description = "服务繁忙")
    @ApiResponse(responseCode = "3002",description = "参数校验失败")
    public Result<Void> updateUser(@RequestBody SysUserDTO userDTO) {
        return null;
    }

    @GetMapping("/detail")
    @Operation(summary = "获取用户详细信息",description = "根据用户ID获取用户详细信息")
    @Parameters(value = {
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "⽤户ID")
    })
    @ApiResponse(responseCode = "1000", description = "成功获取⽤⼾信息")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3101", description = "⽤⼾不存在")
    public Result<SysUserVO> detail(@RequestParam Long userId) {
        return null;
    }
}
