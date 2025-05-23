package com.example.system.service.impl;

import com.example.common.core.constants.CacheConstants;
import com.example.common.core.enums.ResultCode;
import com.example.common.redis.service.RedisService;
import com.example.common.security.exception.ServiceException;
import com.example.system.domain.user.dto.UserQueryDTO;
import com.example.system.domain.user.dto.UserStatusDTO;
import com.example.system.domain.user.entity.User;
import com.example.system.domain.user.vo.UserQueryVO;
import com.example.system.mapper.UserMapper;
import com.example.system.service.UserService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisService redisService;

    @Override
    public List<UserQueryVO> list(UserQueryDTO userQueryDTO) {
        //开启分页插件
        PageHelper.startPage(userQueryDTO.getPageNum(), userQueryDTO.getPageSize());
        return userMapper.getUserList(userQueryDTO);
    }

    @Override
    public int updateStatus(UserStatusDTO userStatusDTO) {
        //先查到 再修改
        User user = userMapper.selectById(userStatusDTO.getUserId());
        if(user == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        user.setStatus(userStatusDTO.getStatus());
        int result = userMapper.updateById(user);
        if(result >= 0) {
            //需要更新用户信息缓存
            redisService.deleteObject(CacheConstants.USER_DETAIL_KEY_PREFIX+userStatusDTO.getUserId());
        }
        return result;
    }
}
