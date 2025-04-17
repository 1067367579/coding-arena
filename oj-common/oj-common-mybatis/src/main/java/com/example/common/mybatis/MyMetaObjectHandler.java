package com.example.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.core.constants.JwtConstants;
import com.example.core.utils.ThreadLocalUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//MyBatisPlus的SQL操作拦截器
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject,"createBy",Long.class, (Long) ThreadLocalUtil.getLocalMap().get(JwtConstants.USER_ID));
        this.strictInsertFill(metaObject,"createTime", LocalDateTime.class,LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject,"updateBy",Long.class,(Long) ThreadLocalUtil.getLocalMap().get(JwtConstants.USER_ID));
        this.strictUpdateFill(metaObject,"updateTime", LocalDateTime.class,LocalDateTime.now());
    }
}
