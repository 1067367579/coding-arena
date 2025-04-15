package com.example.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//MyBatisPlus的SQL操作拦截器
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    //todo 操作人的ID记录 如何获取当前操作用户的ID
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject,"createBy",Long.class,1908547690506072065L);
        this.strictInsertFill(metaObject,"createTime", LocalDateTime.class,LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject,"updateBy",Long.class,1908547690506072065L);
        this.strictUpdateFill(metaObject,"updateTime", LocalDateTime.class,LocalDateTime.now());
    }
}
