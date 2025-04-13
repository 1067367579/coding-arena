package com.example.friend.service;

import com.example.core.domain.Result;
import com.example.friend.domain.dto.SendCodeDTO;

public interface UserService {
    Result<?> sendCode(SendCodeDTO sendCodeDTO);
}
