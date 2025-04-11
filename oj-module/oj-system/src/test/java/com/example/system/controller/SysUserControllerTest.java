package com.example.system.controller;

import com.example.common.redis.service.RedisService;
import com.example.system.domain.user.entity.SysUser;
import com.example.system.mapper.SysUserMapper;
import com.example.system.utils.BCryptUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

//@SpringBootTest(classes = OJSystemApplication.class)
@Slf4j
public class SysUserControllerTest {

    //@Autowired
    SysUserMapper sysUserMapper;

    //@Autowired
    RedisService redisService;

    //@Test
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

    //@Test
    public void testUpdate() {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(1908547690506072065L);
        String encodedPassword = BCryptUtils.encryptPassword("123456");
        sysUser.setPassword(encodedPassword);
        sysUserMapper.updateById(sysUser);
    }

    //@Test
    public void log() {
        log.info("我是info级别的日志");
        log.error("我是error级别的日志");
        log.info(UUID.randomUUID().toString());
    }

    //@Test
    public void redisAddAndGet() {
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount("15119047712");
        sysUser.setPassword("123456");
        sysUser.setNickName("nickname");
        sysUser.setCreateBy(1L);
        sysUser.setUpdateBy(1L);
        sysUser.setCreateTime(LocalDateTime.now());
        sysUser.setUpdateTime(LocalDateTime.now());
        redisService.setCacheObject(sysUser.getUserAccount(),sysUser);
        SysUser cacheUser = redisService.getCacheObject(sysUser.getUserAccount(), SysUser.class);
        System.out.println(cacheUser);
    }
}