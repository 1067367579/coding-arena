package com.example.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.core.domain.Result;
import com.example.core.enums.ResultCode;
import com.example.system.entity.LoginDTO;
import com.example.system.entity.SysUser;
import com.example.system.mapper.SysUserMapper;
import com.example.system.service.SysUserService;
import com.example.system.utils.BCryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Result<Void> login(LoginDTO loginDTO) {
        String userAccount = loginDTO.getUserAccount();
        String password = loginDTO.getPassword();
        if(!StringUtils.hasLength(userAccount) || !StringUtils.hasLength(password)){
            return Result.fail(ResultCode.FAILED_PARAMS_VALIDATE);
        }
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getPassword).eq(SysUser::getUserAccount,userAccount));
        if(user == null){
            return Result.fail(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if(!BCryptUtils.matchesPassword(password,user.getPassword())){
            return Result.fail(ResultCode.FAILED_LOGIN);
        }
        return Result.ok();
    }
}
