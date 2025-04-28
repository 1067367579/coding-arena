package com.example.friend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.constants.JwtConstants;
import com.example.common.core.domain.LoginUser;
import com.example.common.core.domain.Result;
import com.example.common.core.enums.ResultCode;
import com.example.common.core.utils.ThreadLocalUtil;
import com.example.common.file.OSSService;
import com.example.common.message.service.EmailService;
import com.example.common.redis.service.RedisService;
import com.example.common.security.exception.ServiceException;
import com.example.common.security.service.TokenService;
import com.example.friend.domain.dto.SendCodeDTO;
import com.example.friend.domain.dto.UserEditDTO;
import com.example.friend.domain.dto.UserLoginDTO;
import com.example.friend.domain.entity.User;
import com.example.friend.domain.vo.UserVO;
import com.example.friend.manager.UserCacheManager;
import com.example.friend.mapper.UserMapper;
import com.example.friend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
@RefreshScope
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

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private OSSService ossService;

    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserCacheManager userCacheManager;

    @Override
    public Result<?> sendCode(SendCodeDTO sendCodeDTO) {
        //邮箱号已经在传过来的时候进行了校验 获取验证码接口需要进行安全性检验 避免频繁刷新
        //在生成验证码发送前 校验此时验证码的ttl 若上次获取到现在还没超过一分钟 拦截
        Long expire = redisService.getExpire(getCodeKey(sendCodeDTO.getEmail()), TimeUnit.SECONDS);
        if(expire != null && ttl*60-expire < 60) {
            throw new ServiceException(ResultCode.FAILED_CODE_FREQUENT);
        }
        //如果一天内请求的次数大于阈值 也报错 直到第二天才能够再次获取验证码
        Long counter = redisService.getCacheObject(getCountKey(sendCodeDTO.getEmail()),Long.class);
        if(counter != null && counter >= countLimit) {
            throw new ServiceException(ResultCode.FAILED_CODE_FREQUENT);
        }
        //生成验证码 6位 纯数字
        String code = RandomUtil.randomNumbers(6);
        //存入redis中
        redisService.setCacheObject(getCodeKey(sendCodeDTO.getEmail()), code
            ,ttl, TimeUnit.MINUTES);
        //邮箱发送服务
        emailService.sendSimpleMail(sendCodeDTO.getEmail(),code);
        //加一次成功获取次数
        redisService.increment(getCountKey(sendCodeDTO.getEmail()));
        //如果之前获取到的计数器为null 证明是当天第一次发送验证码请求 设置key的ttl到明天凌晨
        if(counter == null) {
            long lifespan = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0)
                            .withNano(0));
            redisService.expire(getCountKey(sendCodeDTO.getEmail()),lifespan,TimeUnit.SECONDS);
        }
        //判断当前的验证码的key
        return Result.ok();
    }

    @Override
    public String login(UserLoginDTO loginDTO) {
        checkCode(loginDTO);
        //校对成功 验证码正确
        //判断是新用户还是老用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, loginDTO.getEmail()));
        if(user == null) {
            //新用户 要执行插入逻辑
            user = User.defaultUser(loginDTO.getEmail());
            userMapper.insert(user);
        }
        //生成令牌 存入redis中
        return tokenService.createToken(user.getUserId(), user.getNickName(),
                user.getAvatar(),
                CacheConstants.LOGIN_IDENTITY_USER, secret);
    }

    @Override
    public boolean logout(String token) {
        //在网关放行不需要登录就可以使用的接口 接口标注了semiLogin
        boolean res;
        try {
            res = tokenService.deleteLoginUser(token,secret);
        } catch (Exception e) {
            throw new ServiceException(ResultCode.FAILED);
        }
        return res;
    }

    @Override
    public Result<LoginUser> info(String token) {
        LoginUser loginUser = tokenService.getLoginUser(token, secret);
        //从令牌中获取出登录用户
        return loginUser==null?Result.fail():Result.ok(loginUser);
    }

    @Override
    public UserVO detail() {
        Long userId = getUserId();
        //查询redis
        return userCacheManager.getUserDetail(userId);
    }

    public Long getUserId() {
        //查用户ID
        Object userIdObject = ThreadLocalUtil.getLocalMap().get(JwtConstants.USER_ID);
        if(userIdObject == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        //如果查到了用户ID
        return (Long) userIdObject;
    }

    @Override
    public int edit(UserEditDTO userEditDTO) {
        Long userId = getUserId();
        //从数据库中获取
        User user = userMapper.selectById(userId);
        if(user == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        //修改用户
        user.setNickName(userEditDTO.getNickName());
        user.setAvatar(userEditDTO.getAvatar());
        user.setSchool(userEditDTO.getSchool());
        user.setGender(userEditDTO.getGender());
        user.setMajor(userEditDTO.getMajor());
        user.setIntroduce(userEditDTO.getIntroduce());
        user.setWechat(userEditDTO.getWechat());
        user.setPhone(userEditDTO.getPhone());
        //更新数据库以及缓存
        int result = userMapper.updateById(user);
        if(result >= 0) {
            //更新数据库成功 刷新LoginUser缓存和UserDetail缓存
            userCacheManager.refreshUserDetail(userId);
            //获取LoginUser
            LoginUser loginUser = redisService.getCacheObject(CacheConstants.USER_TOKEN_PREFIX + userId, LoginUser.class);
            loginUser.setNickName(userEditDTO.getNickName());
            loginUser.setAvatar(userEditDTO.getAvatar());
            redisService.setCacheObject(CacheConstants.USER_TOKEN_PREFIX + userId,loginUser);
        }
        return result;
    }

    @Override
    public int updateAvatar(UserEditDTO userEditDTO) {
        Long userId = getUserId();
        //从数据库中获取
        User user = userMapper.selectById(userId);
        if(user == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        user.setAvatar(userEditDTO.getAvatar());
        //更新数据库以及缓存
        int result = userMapper.updateById(user);
        if(result >= 0) {
            //更新数据库成功 刷新LoginUser缓存和UserDetail缓存
            userCacheManager.refreshUserDetail(userId);
            //获取LoginUser
            LoginUser loginUser = redisService.getCacheObject(CacheConstants.USER_TOKEN_PREFIX + userId, LoginUser.class);
            loginUser.setAvatar(userEditDTO.getAvatar());
            redisService.setCacheObject(CacheConstants.USER_TOKEN_PREFIX + userId,loginUser);
        }
        return result;
    }

    private void checkCode(UserLoginDTO loginDTO) {
        //先校对验证码
        String code = loginDTO.getCode();
        String rightCode = redisService.getCacheObject(getCodeKey(loginDTO.getEmail()), String.class);
        if(rightCode == null) {
            throw new ServiceException(ResultCode.FAILED_CODE_INVALID);
        }
        if(!code.equals(rightCode)) {
            throw new ServiceException(ResultCode.FAILED_CODE_WRONG);
        }
        //完成校对逻辑之后 这个redis中的验证码就无用了
        redisService.deleteObject(getCodeKey(loginDTO.getEmail()));
    }

    private static String getCodeKey(String email) {
        return CacheConstants.EMAIL_CODE_KEY_PREFIX + email;
    }

    private static String getCountKey(String email) {
        return CacheConstants.CODE_COUNTER_KEY_PREFIX + email;
    }
}
