package com.example.system.controller;

import com.example.system.OJSystemApplication;
import com.example.system.entity.SysUser;
import com.example.system.mapper.SysUserMapper;
import com.example.system.utils.BCryptUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = OJSystemApplication.class)
public class SysUserControllerTest {

    @Autowired
    SysUserMapper sysUserMapper;

    @Test
    public void testAdd() {
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount("15119047712");
        sysUser.setPassword("123456");
        sysUser.setNickName("nickname");
        sysUser.setCreateBy(1L);
        sysUser.setUpdateBy(1L);
        int inserted = sysUserMapper.insert(sysUser);
        System.out.println("插入成功的数量: "+inserted);
    }

    @Test
    public void testUpdate() {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(1908547690506072065L);
        String encodedPassword = BCryptUtils.encryptPassword("123456");
        sysUser.setPassword(encodedPassword);
        sysUserMapper.updateById(sysUser);
    }
}