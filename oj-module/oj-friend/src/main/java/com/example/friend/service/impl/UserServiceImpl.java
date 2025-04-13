package com.example.friend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.example.common.message.service.EmailService;
import com.example.common.redis.service.RedisService;
import com.example.common.security.exception.ServiceException;
import com.example.core.constants.RedisConstants;
import com.example.core.domain.Result;
import com.example.core.enums.ResultCode;
import com.example.friend.domain.dto.SendCodeDTO;
import com.example.friend.mapper.UserMapper;
import com.example.friend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisService redisService;

    @Value("${email.ttl}")
    private Long ttl;

    @Value("${email.count}")
    private Long countLimit;

    @Override
    public Result<?> sendCode(SendCodeDTO sendCodeDTO) {
        //邮箱号已经在传过来的时候进行了校验 获取验证码接口需要进行安全性检验 避免频繁刷新
        //在生成验证码发送前 校验此时验证码的ttl 若上次获取到现在还没超过一分钟 拦截
        Long expire = redisService.getExpire(getCodeKey(sendCodeDTO), TimeUnit.SECONDS);
        if(expire != null && ttl*60-expire < 60) {
            throw new ServiceException(ResultCode.FAILED_CODE_FREQUENT);
        }
        //如果一天内请求的次数大于阈值 也报错 直到第二天才能够再次获取验证码
        Long counter = redisService.getCacheObject(getCountKey(sendCodeDTO),Long.class);
        if(counter != null && counter >= countLimit) {
            throw new ServiceException(ResultCode.FAILED_CODE_FREQUENT);
        }
        //生成验证码 6位 纯数字
        String code = RandomUtil.randomNumbers(6);
        //存入redis中
        redisService.setCacheObject(getCodeKey(sendCodeDTO), code
            ,ttl, TimeUnit.MINUTES);
        //邮箱发送服务
        emailService.sendSimpleMail(sendCodeDTO.getEmail(),code);
        //加一次成功获取次数
        redisService.increment(getCountKey(sendCodeDTO));
        //如果之前获取到的计数器为null 证明是当天第一次发送验证码请求 设置key的ttl到明天凌晨
        if(counter == null) {
            long lifespan = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
            redisService.expire(getCountKey(sendCodeDTO),lifespan,TimeUnit.SECONDS);
        }
        //判断当前的验证码的key
        return Result.ok();
    }

    private static String getCodeKey(SendCodeDTO sendCodeDTO) {
        return RedisConstants.EMAIL_CODE_PREFIX + sendCodeDTO.getEmail();
    }

    private static String getCountKey(SendCodeDTO sendCodeDTO) {
        return RedisConstants.CODE_TIME_KEY + sendCodeDTO.getEmail();
    }
}
