package com.example.friend.manager;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.core.constants.CacheConstants;
import com.example.common.core.enums.ResultCode;
import com.example.common.redis.service.RedisService;
import com.example.common.security.exception.ServiceException;
import com.example.friend.domain.entity.User;
import com.example.friend.domain.vo.UserVO;
import com.example.friend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserCacheManager {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;


    public UserVO getUserDetail(Long userId) {
        //获取用户信息的key
        String userDetailKey = getUserDetailKey(userId);
        //获取redis
        UserVO userVO = redisService.getCacheObject(userDetailKey, UserVO.class);
        if(userVO == null) {
            userVO = refreshUserDetail(userId);
        }
        //一次获取延续10分钟缓存
        redisService.expire(getUserDetailKey(userId),10L, TimeUnit.MINUTES);
        //从redis中成功获取了数据 返回 并设置TTL
        return userVO;
    }

    public UserVO refreshUserDetail(Long userId) {
        //如果user为null 到 mysql中刷新
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getUserId, User::getEmail, User::getNickName,
                        User::getAvatar, User::getGender, User::getMajor,
                        User::getSchool, User::getIntroduce, User::getPhone, User::getWechat)
                .eq(User::getUserId, userId)
        );
        //还要检测mysql中是否有数据
        if(user == null) {
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        //有数据 刷新redis 并设置TTL为10分钟
        UserVO userVO = BeanUtil.toBean(user, UserVO.class);
        redisService.setCacheObject(getUserDetailKey(userId), userVO);
        return userVO;
    }

    public String getUserDetailKey(Long userId) {
        return CacheConstants.USER_DETAIL_KEY_PREFIX+userId;
    }
}
