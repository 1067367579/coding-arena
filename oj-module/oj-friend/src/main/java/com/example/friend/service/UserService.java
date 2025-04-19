package com.example.friend.service;

import com.example.common.core.domain.LoginUser;
import com.example.common.core.domain.Result;
import com.example.common.file.OSSResult;
import com.example.friend.domain.dto.SendCodeDTO;
import com.example.friend.domain.dto.UserEditDTO;
import com.example.friend.domain.dto.UserLoginDTO;
import com.example.friend.domain.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    Result<?> sendCode(SendCodeDTO sendCodeDTO);

    String login(UserLoginDTO loginDTO);

    boolean logout(String token);

    Result<LoginUser> info(String token);

    UserVO detail();

    int edit(UserEditDTO userEditDTO);

    int updateAvatar(UserEditDTO userEditDTO);
}
