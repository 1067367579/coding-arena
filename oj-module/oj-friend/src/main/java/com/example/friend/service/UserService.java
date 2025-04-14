package com.example.friend.service;

import com.example.core.domain.LoginUser;
import com.example.core.domain.Result;
import com.example.friend.domain.dto.SendCodeDTO;
import com.example.friend.domain.dto.UserLoginDTO;

public interface UserService {
    Result<?> sendCode(SendCodeDTO sendCodeDTO);

    String login(UserLoginDTO loginDTO);

    boolean logout(String token);

    Result<LoginUser> info(String token);
}
