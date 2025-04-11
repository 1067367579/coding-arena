package com.example.system.service;

import com.example.core.domain.Result;
import com.example.system.domain.user.dto.LoginDTO;
import com.example.system.domain.user.dto.SysUserDTO;
import com.example.system.domain.user.vo.LoginUserVO;

public interface SysUserService {
    Result<String> login(LoginDTO loginDTO);

    int add(SysUserDTO userDTO);

    Result<LoginUserVO> info(String token);

    boolean logout(String token);
}
