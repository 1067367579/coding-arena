package com.example.system.service;

import com.example.system.domain.user.dto.UserQueryDTO;
import com.example.system.domain.user.dto.UserStatusDTO;

import java.util.List;

public interface UserService {
    List<?> list(UserQueryDTO userQueryDTO);

    int updateStatus(UserStatusDTO userStatusDTO);
}
