package com.example.system.service;

import com.example.core.domain.Result;
import com.example.system.domain.dto.LoginDTO;
import com.example.system.domain.dto.SysUserDTO;

public interface SysUserService {
    Result<String> login(LoginDTO loginDTO);

    int add(SysUserDTO userDTO);
}
