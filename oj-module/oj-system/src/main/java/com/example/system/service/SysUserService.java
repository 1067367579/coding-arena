package com.example.system.service;

import com.example.core.domain.Result;
import com.example.system.entity.LoginDTO;

public interface SysUserService {
    Result<String> login(LoginDTO loginDTO);

}
