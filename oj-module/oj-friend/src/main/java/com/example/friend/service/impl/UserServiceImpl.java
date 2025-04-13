package com.example.friend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.example.common.message.service.EmailService;
import com.example.common.redis.service.RedisService;
import com.example.common.security.exception.ServiceException;
import com.example.core.domain.Result;
import com.example.core.enums.ResultCode;
import com.example.friend.domain.dto.SendCodeDTO;
import com.example.friend.mapper.UserMapper;
import com.example.friend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisService redisService;

    @Override
    public Result<?> sendCode(SendCodeDTO sendCodeDTO) {
        //邮箱号已经在传过来的时候进行了校验
        //生成验证码 6位 纯数字
        String code = RandomUtil.randomNumbers(6);
        try{
            emailService.sendSimpleMail(sendCodeDTO.getEmail(),code);
        } catch (Exception e) {
            log.error("发送验证码操作失败！");
            throw new ServiceException(ResultCode.FAILED);
        }
        return Result.ok();
    }
}
