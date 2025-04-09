package com.example.system.service;

import com.example.core.domain.Result;
import com.example.system.entity.LoginDTO;
import com.example.system.entity.SysUserDTO;

public interface SysUserService {
    Result<String> login(LoginDTO loginDTO);

    int add(SysUserDTO userDTO);
}
