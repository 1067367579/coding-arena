package com.example.system.service.impl;

import com.example.system.domain.user.dto.UserQueryDTO;
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

    @Override
    public List<UserQueryVO> list(UserQueryDTO userQueryDTO) {
        //开启分页插件
        PageHelper.startPage(userQueryDTO.getPageNum(), userQueryDTO.getPageSize());
        return userMapper.getUserList(userQueryDTO);
    }
}
